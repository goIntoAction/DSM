package wang.zengye.dsm.terminal

import android.util.Log
import androidx.compose.ui.graphics.Color
import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import com.trilead.ssh2.ConnectionMonitor
import com.trilead.ssh2.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.connectbot.terminal.TerminalEmulator
import org.connectbot.terminal.TerminalEmulatorFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * SSH 终端桥接器：将 sshlib 连接与 termlib 终端模拟器连接起来
 * 完全参考 ConnectBot 的 TerminalBridge + Relay + SSH transport 架构
 */
class SshTerminalBridge {

    companion object {
        private const val TAG = "SshTerminalBridge"
        private const val BUFFER_SIZE = 4096
        private const val CONNECTION_TIMEOUT = 30_000
        private const val KEX_TIMEOUT = 30_000
        private const val DEFAULT_COLS = 80
        private const val DEFAULT_ROWS = 24

        // 与 ConnectBot SSH.kt conditions 保持一致
        private const val CONDITIONS = (
            ChannelCondition.STDOUT_DATA
                or ChannelCondition.STDERR_DATA
                or ChannelCondition.CLOSED
                or ChannelCondition.EOF
            )
    }

    sealed class State {
        data object Idle : State()
        data object Connecting : State()
        data class Connected(val host: String, val username: String) : State()
        data class Error(val message: String) : State()
        data object Disconnected : State()
    }

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // 与 ConnectBot 一致：用 Channel 串行化所有写操作，避免并发写 stdin
    private sealed class TransportOperation {
        data class WriteData(val data: ByteArray) : TransportOperation()
        data class SetDimensions(val columns: Int, val rows: Int, val width: Int, val height: Int) : TransportOperation()
    }

    private val transportOperations = Channel<TransportOperation>(Channel.UNLIMITED)

    // 修饰键管理器 — 用于支持 Ctrl/Alt/Shift 修饰键
    val modifierManager = TerminalModifierManager()

    // termlib 终端模拟器 — 处理所有 VT100/xterm 转义序列
    val terminalEmulator: TerminalEmulator = TerminalEmulatorFactory.create(
        initialRows = DEFAULT_ROWS,
        initialCols = DEFAULT_COLS,
        defaultForeground = Color.White,
        defaultBackground = Color(0xFF1E1E1E),
        onKeyboardInput = { data ->
            // 与 ConnectBot 一致：trySend 非阻塞投递，由 transportOperationProcessor 串行处理
            transportOperations.trySend(TransportOperation.WriteData(data))
        },
        onBell = { /* 可选：震动/声音 */ },
        onResize = { dims ->
            // 与 ConnectBot 一致：异步处理 resizePTY，避免主线程网络操作
            transportOperations.trySend(
                TransportOperation.SetDimensions(dims.columns, dims.rows, 0, 0)
            )
        },
        onClipboardCopy = { /* 可选：剪贴板 */ },
        onProgressChange = { _, _ -> }
    )

    private val _state = MutableStateFlow<State>(State.Idle)
    val state: StateFlow<State> = _state.asStateFlow()

    // SSH 底层资源
    private var connection: Connection? = null
    private var session: Session? = null
    private var stdin: OutputStream? = null
    private var stdout: InputStream? = null
    private var stderr: InputStream? = null
    private var relayJob: Job? = null
    private var writeProcessorJob: Job? = null

    private var currentCols = DEFAULT_COLS
    private var currentRows = DEFAULT_ROWS

    // ── 公开 API ──────────────────────────────────────────────

    /**
     * 连接到 SSH 服务器（密码认证）
     */
    suspend fun connect(
        host: String,
        port: Int,
        username: String,
        password: String
    ) = withContext(Dispatchers.IO) {
        _state.value = State.Connecting

        try {
            val conn = Connection(host, port)
            conn.addConnectionMonitor(ConnectionMonitor {
                if (_state.value is State.Connected) {
                    _state.value = State.Disconnected
                }
            })
            conn.connect(null, CONNECTION_TIMEOUT, KEX_TIMEOUT)
            connection = conn

            val authenticated = conn.authenticateWithPassword(username, password)
            if (!authenticated) {
                conn.close()
                connection = null
                _state.value = State.Error("认证失败：用户名或密码错误")
                return@withContext
            }

            val sess = conn.openSession()
            sess.requestPTY("xterm", currentCols, currentRows, 0, 0, null)
            sess.startShell()
            session = sess
            stdin = sess.stdin
            stdout = sess.stdout
            stderr = sess.stderr

            _state.value = State.Connected(host, username)
            Log.d(TAG, "Connected to $host:$port")

            // 与 ConnectBot 一致：先启动传输操作处理器，再启动 relay
            startTransportOperationProcessor()
            startRelay()

        } catch (e: Exception) {
            Log.e(TAG, "Connection failed", e)
            cleanup()
            _state.value = State.Error(e.message ?: "连接失败")
        }
    }

    /**
     * 断开连接
     */
    fun disconnect() {
        cleanup()
        _state.value = State.Disconnected
    }

    /**
     * 将用户输入的字符串发送到 SSH（原始字节，UTF-8 编码���
     */
    fun sendString(text: String) {
        transportOperations.trySend(
            TransportOperation.WriteData(text.toByteArray(Charsets.UTF_8))
        )
    }

    /**
     * 调整终端大小
     */
    fun resize(cols: Int, rows: Int) {
        if (cols == currentCols && rows == currentRows) return
        currentCols = cols
        currentRows = rows
        terminalEmulator.resize(cols, rows)
    }

    /**
     * 释放所有资源
     */
    fun release() {
        cleanup()
        scope.cancel()
    }

    // ── 内部实现 ──────────────────────────────────────────────

    /**
     * 传输操作处理器：与 ConnectBot startTransportOperationProcessor 一致
     * 串行消费 transportOperations，保证所有网络操作的正确顺序
     * 在 IO 线程执行，避免 NetworkOnMainThreadException
     */
    private fun startTransportOperationProcessor() {
        writeProcessorJob?.cancel()
        writeProcessorJob = scope.launch(Dispatchers.IO) {
            for (operation in transportOperations) {
                try {
                    when (operation) {
                        is TransportOperation.WriteData -> {
                            stdin?.write(operation.data)
                            stdin?.flush()
                        }
                        is TransportOperation.SetDimensions -> {
                            session?.resizePTY(
                                operation.columns,
                                operation.rows,
                                operation.width,
                                operation.height
                            )
                        }
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Failed to process transport operation", e)
                }
            }
        }
    }

    /**
     * 数据中继协程：与 ConnectBot Relay.start() + SSH.read() 完全一致
     * 用 waitForCondition 监听 stdout/stderr/EOF，只转发 stdout 数据到终端
     */
    private fun startRelay() {
        val currentSession = session ?: return
        relayJob?.cancel()

        relayJob = scope.launch(Dispatchers.IO) {
            val buf = ByteArray(BUFFER_SIZE)
            val discard = ByteArray(256)

            try {
                while (isActive) {
                    // 与 ConnectBot SSH.read() 完全一致：waitForCondition 阻塞等待
                    val conditions = currentSession.waitForCondition(CONDITIONS, 0)

                    if ((conditions and ChannelCondition.STDOUT_DATA) != 0) {
                        val read = stdout?.read(buf, 0, buf.size) ?: 0
                        if (read > 0) {
                            terminalEmulator.writeInput(buf, 0, read)
                        }
                    }

                    // 与 ConnectBot 一致：消费 stderr 防止缓冲区阻塞，但不显示
                    if ((conditions and ChannelCondition.STDERR_DATA) != 0) {
                        while ((stderr?.available() ?: 0) > 0) {
                            stderr?.read(discard)
                        }
                    }

                    if ((conditions and (ChannelCondition.EOF or ChannelCondition.CLOSED)) != 0) {
                        break
                    }
                }
            } catch (e: IOException) {
                if (isActive) {
                    Log.e(TAG, "Relay error", e)
                }
            }

            if (isActive && _state.value is State.Connected) {
                _state.value = State.Disconnected
            }
        }
    }

    private fun cleanup() {
        relayJob?.cancel()
        relayJob = null
        writeProcessorJob?.cancel()
        writeProcessorJob = null
        transportOperations.close()
        try {
            stdin?.close()
            stdout?.close()
            stderr?.close()
            session?.close()
            connection?.close()
        } catch (_: Exception) {}
        stdin = null
        stdout = null
        stderr = null
        session = null
        connection = null
    }
}
