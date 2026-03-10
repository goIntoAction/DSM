package wang.zengye.dsm.data.api

import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import java.io.File
import java.io.IOException

/**
 * 支持进度追踪的 RequestBody
 * 用于文件上传时显示进度
 */
class ProgressRequestBody(
    private val file: File,
    private val contentType: MediaType = "application/octet-stream".toMediaType(),
    private val progressCallback: ((bytesWritten: Long, contentLength: Long) -> Unit)? = null,
    private val isCancelled: (() -> Boolean)? = null
) : RequestBody() {

    override fun contentType() = contentType

    override fun contentLength() = file.length()

    override fun writeTo(sink: BufferedSink) {
        val contentLength = contentLength()
        val progressSink = object : ForwardingSink(sink) {
            private var bytesWritten: Long = 0

            override fun write(source: okio.Buffer, byteCount: Long) {
                isCancelled?.let {
                    if (it()) throw IOException("Upload cancelled")
                }
                super.write(source, byteCount)
                bytesWritten += byteCount
                progressCallback?.invoke(bytesWritten, contentLength)
            }
        }

        // 使用 delegate sink，不要手动关闭
        val bufferedSink = progressSink.buffer()
        file.inputStream().use { input ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (input.read(buffer).also { bytesRead = it } != -1) {
                isCancelled?.let {
                    if (it()) throw IOException("Upload cancelled")
                }
                bufferedSink.write(buffer, 0, bytesRead)
            }
        }
        // 刷新缓冲区，但不关闭 sink（由 OkHttp 管理）
        bufferedSink.emit()
    }
}
