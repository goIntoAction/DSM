package wang.zengye.dsm.util

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

/**
 * 权限请求协调器
 * 使用单例模式，让 ViewModel 可以请求权限，而无需直接依赖 Activity
 */
object PermissionRequester {

    /**
     * 权限请求结果
     */
    data class PermissionResult(
        val permission: String,
        val isGranted: Boolean,
        val shouldShowRationale: Boolean
    )

    private val _permissionResultChannel = Channel<PermissionResult>(Channel.UNLIMITED)
    val permissionResultFlow = _permissionResultChannel.receiveAsFlow()

    private var activityRef: ComponentActivity? = null
    private var permissionLauncher: PermissionLauncher? = null

    /**
     * 注册 Activity（在 MainActivity 的 onCreate 中调用）
     */
    fun registerActivity(activity: ComponentActivity, launcher: PermissionLauncher) {
        activityRef = activity
        permissionLauncher = launcher
    }

    /**
     * 注销 Activity（在 MainActivity 的 onDestroy 中调用）
     */
    fun unregisterActivity() {
        activityRef = null
        permissionLauncher = null
    }

    /**
     * 检查是否有通知权限
     */
    fun hasNotificationPermission(): Boolean {
        val activity = activityRef ?: return true // 如果没有 Activity，假设有权限
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                activity,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * 请求通知权限
     * @return true 表示请求已发出，false 表示无法请求（没有 Activity 或不需要权限）
     */
    fun requestNotificationPermission(): Boolean {
        val launcher = permissionLauncher ?: return false
        val activity = activityRef ?: return false

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            // 不需要权限，直接返回成功
            notifyResult(PermissionResult(
                permission = Manifest.permission.POST_NOTIFICATIONS,
                isGranted = true,
                shouldShowRationale = false
            ))
            return true
        }

        launcher.requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS))
        return true
    }

    /**
     * 通知权限结果（由 Activity 的 launcher 回调中调用）
     */
    fun notifyResult(result: PermissionResult) {
        CoroutineScope(Dispatchers.Default).launch {
            _permissionResultChannel.send(result)
        }
    }

    /**
     * 权限启动器接口，由 Activity 实现
     */
    interface PermissionLauncher {
        fun requestPermissions(permissions: Array<String>)
    }
}

/**
 * 在 Composable 中创建权限启动器
 */
@Composable
fun rememberPermissionLauncher(
    onResult: (Map<String, Boolean>) -> Unit = {}
): PermissionRequester.PermissionLauncher {
    val activity = androidx.activity.compose.LocalActivity.current
        ?: return remember { object : PermissionRequester.PermissionLauncher {
            override fun requestPermissions(permissions: Array<String>) {}
        }}

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        onResult(result)
        // 通知 PermissionRequester
        result.forEach { (permission, isGranted) ->
            val shouldShowRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            PermissionRequester.notifyResult(PermissionRequester.PermissionResult(
                permission = permission,
                isGranted = isGranted,
                shouldShowRationale = shouldShowRationale
            ))
        }
    }

    return remember(activity) {
        object : PermissionRequester.PermissionLauncher {
            override fun requestPermissions(permissions: Array<String>) {
                // 检查是否已经有所有权限
                val allGranted = permissions.all { 
                    ContextCompat.checkSelfPermission(activity, it) == PackageManager.PERMISSION_GRANTED 
                }
                if (allGranted) {
                    // 已经有权限，直接通知
                    permissions.forEach { permission ->
                        PermissionRequester.notifyResult(PermissionRequester.PermissionResult(
                            permission = permission,
                            isGranted = true,
                            shouldShowRationale = false
                        ))
                    }
                    return
                }
                launcher.launch(permissions)
            }
        }
    }
}