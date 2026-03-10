package wang.zengye.dsm

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.api.DsmApiHelper
import wang.zengye.dsm.navigation.DSMNavHost
import wang.zengye.dsm.navigation.DsmRoute
import wang.zengye.dsm.ui.theme.DSMTheme
import wang.zengye.dsm.util.BiometricHelper
import wang.zengye.dsm.util.SettingsManager
import wang.zengye.dsm.util.PermissionRequester
import wang.zengye.dsm.util.rememberPermissionLauncher
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var isAuthenticating = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            DSMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    // 注册权限启动器到 PermissionRequester
                    val permissionLauncher = rememberPermissionLauncher()
                    DisposableEffect(permissionLauncher) {
                        PermissionRequester.registerActivity(this@MainActivity, permissionLauncher)
                        onDispose {
                            PermissionRequester.unregisterActivity()
                        }
                    }

                    // 初始化 API 客户端并检查启动认证
                    LaunchedEffect(Unit) {
                        DsmApiHelper.init()
                        checkLaunchAuth()
                    }

                    // 监听会话过期事件，自动重登录失败时跳转登录页
                    LaunchedEffect(navController) {
                        DsmApiHelper.sessionExpiredEvent
                            .onEach {
                                Log.w(TAG, "Session expired and re-login failed, navigating to login")
                                navController.navigate(DsmRoute.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .launchIn(this)
                    }

                    DSMNavHost(
                        navController = navController,
                        onAuthRequired = { showBiometricAuth() }
                    )
                }
            }
        }
    }

    /**
     * 检查是否需要启动认证
     */
    private fun checkLaunchAuth() {
        lifecycleScope.launch {
            try {
                val launchAuth = SettingsManager.launchAuth.first()
                val savedSid = SettingsManager.sid.value
                val savedHost = SettingsManager.host.first()
                val savedAccount = SettingsManager.account.first()

                // 如果没有保存会话，直接跳过
                if (savedSid.isEmpty() || savedHost.isEmpty() || savedAccount.isEmpty()) {
                    Log.d(TAG, "No saved session, skipping launch auth")
                    return@launch
                }

                // 如果启用了启动认证
                if (launchAuth && !isAuthenticating) {
                    isAuthenticating = true
                    showBiometricAuth()
                } else {
                    // 直接恢复会话
                    restoreSession()
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking launch auth", e)
            }
        }
    }

    /**
     * 显示系统认证（生物识别或设备凭据）
     */
    private fun showBiometricAuth() {
        val authStatus = BiometricHelper.isAuthAvailable(this)

        when (authStatus) {
            BiometricHelper.AuthStatus.AVAILABLE -> {
                BiometricHelper.showAuthPrompt(
                    activity = this,
                    title = getString(R.string.biometric_title),
                    subtitle = getString(R.string.biometric_subtitle_auto_login),
                    onSuccess = {
                        Log.d(TAG, "Auth succeeded")
                        restoreSession()
                    },
                    onError = { error ->
                        Log.e(TAG, "Auth error: $error")
                        // 验证失败，不自动登录
                        isAuthenticating = false
                    },
                    onCancel = {
                        Log.d(TAG, "Auth cancelled")
                        isAuthenticating = false
                    }
                )
            }
            else -> {
                // 没有可用的认证方式，直接恢复会话
                Log.d(TAG, "No auth available, restoring session")
                restoreSession()
            }
        }
    }

    /**
     * 恢复会话
     */
    private fun restoreSession() {
        lifecycleScope.launch {
            try {
                val savedSid = SettingsManager.sid.value
                val savedCookie = SettingsManager.cookie.value
                val savedHost = SettingsManager.host.first()
                val savedSynoToken = SettingsManager.synoToken.value

                if (savedSid.isNotEmpty() && savedHost.isNotEmpty()) {
                    DsmApiHelper.updateSession(savedSid, savedCookie, savedHost)
                    if (savedSynoToken.isNotEmpty()) {
                        DsmApiHelper.updateSynoToken(savedSynoToken)
                    }
                    Log.d(TAG, "Session restored successfully")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error restoring session", e)
            }
            isAuthenticating = false
        }
    }
}
