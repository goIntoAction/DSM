package wang.zengye.dsm

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
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

    // 认证状态
    // null: 检查中
    // "checking": 正在显示生物识别
    // "success": 生物识别成功，可以显示登录页
    // "failed": 生物识别失败/取消，需要重试
    // "skip": 无需生物识别，直接进入
    private var authState by mutableStateOf<String?>(null)
    private var authError by mutableStateOf<String?>(null)
    private var hasCheckedAuth = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: starting")

        enableEdgeToEdge()

        setContent {
            DSMTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()

                    val permissionLauncher = rememberPermissionLauncher()
                    DisposableEffect(permissionLauncher) {
                        PermissionRequester.registerActivity(this@MainActivity, permissionLauncher)
                        onDispose {
                            PermissionRequester.unregisterActivity()
                        }
                    }

                    LaunchedEffect(Unit) {
                        DsmApiHelper.init()
                    }

                    LaunchedEffect(navController) {
                        DsmApiHelper.sessionExpiredEvent
                            .onEach {
                                Log.w(TAG, "Session expired, navigating to login")
                                navController.navigate(DsmRoute.Login) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                            .launchIn(this)
                    }

                    // 添加日志跟踪状态变化
                    LaunchedEffect(authState) {
                        Log.d(TAG, "authState changed to: $authState")
                    }

                    when (authState) {
                        null, "checking" -> {
                            Log.d(TAG, "Showing loading indicator, authState=$authState")
                            // 检查中或等待生物识别
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        "failed" -> {
                            Log.d(TAG, "Showing auth failed screen")
                            // 生物识别失败，显示重试界面
                            AuthFailedScreen(
                                error = authError,
                                onRetry = {
                                    authError = null
                                    authState = "checking"
                                    showBiometricAuth()
                                }
                            )
                        }
                        "success", "skip" -> {
                            Log.d(TAG, "Showing DSMNavHost, authState=$authState")
                            // 认证成功或无需认证，显示导航
                            DSMNavHost(navController = navController)
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!hasCheckedAuth) {
            hasCheckedAuth = true
            // 延迟调用，确保 Activity 完全准备好显示生物识别对话框
            window.decorView.post {
                checkLaunchAuth()
            }
        }
    }

    private fun checkLaunchAuth() {
        lifecycleScope.launch {
            try {
                val launchAuth = SettingsManager.launchAuth.first()

                Log.d(TAG, "checkLaunchAuth: launchAuth=$launchAuth")

                // 启用了启动认证，显示生物识别
                if (launchAuth) {
                    Log.d(TAG, "Launch auth enabled, showing biometric")
                    authState = "checking"
                    showBiometricAuth()
                } else {
                    // 未启用认证，直接进入登录页
                    Log.d(TAG, "Launch auth disabled, going to login")
                    authState = "skip"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking launch auth", e)
                authState = "skip"
            }
        }
    }

    private fun showBiometricAuth() {
        val authStatus = BiometricHelper.isAuthAvailable(this)
        Log.d(TAG, "showBiometricAuth: authStatus=$authStatus")

        when (authStatus) {
            BiometricHelper.AuthStatus.AVAILABLE -> {
                BiometricHelper.showAuthPrompt(
                    activity = this,
                    title = getString(R.string.biometric_title),
                    subtitle = getString(R.string.biometric_subtitle_auto_login),
                    onSuccess = {
                        Log.d(TAG, "Biometric auth succeeded")
                        // 生物识别成功，进入登录页
                        authState = "success"
                    },
                    onError = { error ->
                        Log.e(TAG, "Biometric auth error: $error")
                        authError = error
                        authState = "failed"
                    },
                    onCancel = {
                        Log.d(TAG, "Biometric auth cancelled")
                        authError = getString(R.string.biometric_cancelled)
                        authState = "failed"
                    }
                )
            }
            else -> {
                // 设备不支持生物识别，直接进入登录页
                Log.d(TAG, "Biometric not available, going to login")
                authState = "skip"
            }
        }
    }
}

@Composable
private fun AuthFailedScreen(
    error: String?,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Fingerprint,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.error
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.biometric_auth_failed_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = error ?: stringResource(R.string.biometric_auth_failed_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.biometric_retry))
        }
    }
}
