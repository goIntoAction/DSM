package wang.zengye.dsm.util

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import wang.zengye.dsm.R

/**
 * 系统认证辅助类
 * 支持生物识别（指纹/面容）或设备凭据（PIN/图案/密码）
 */
object BiometricHelper {

    // 支持生物识别强认证或设备凭据
    private val AUTHENTICATORS = BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.DEVICE_CREDENTIAL

    /**
     * 检查设备是否支持系统认证
     */
    fun isAuthAvailable(context: Context): AuthStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> AuthStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> AuthStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> AuthStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> AuthStatus.NOT_ENROLLED
            else -> AuthStatus.UNKNOWN
        }
    }

    /**
     * 检查设备是否支持生物识别（仅生物识别，不含设备凭据）
     */
    fun isBiometricAvailable(context: Context): BiometricStatus {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            else -> BiometricStatus.UNKNOWN
        }
    }

    /**
     * 显示系统认证对话框
     * 优先使用生物识别，如果没有则使用设备凭据（PIN/图案/密码）
     */
    fun showAuthPrompt(
        activity: FragmentActivity,
        title: String = appString(R.string.biometric_title),
        subtitle: String = appString(R.string.biometric_subtitle),
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                if (errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
                    onCancel()
                } else {
                    onError(errString.toString())
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // 用户验证失败，但还可以继续尝试
            }
        }

        val biometricPrompt = BiometricPrompt(activity, executor, callback)

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setAllowedAuthenticators(AUTHENTICATORS)

        // 注意：当 AUTHENTICATORS 包含 DEVICE_CREDENTIAL 时，不能设置 negative button
        // 因为系统会自动显示"使用PIN/图案/密码"选项
        // 所以这里不设置 negative button

        biometricPrompt.authenticate(promptInfoBuilder.build())
    }

    /**
     * 旧方法名兼容，调用新的 showAuthPrompt
     */
    fun showBiometricPrompt(
        activity: FragmentActivity,
        title: String = appString(R.string.biometric_title),
        subtitle: String = appString(R.string.biometric_subtitle),
        negativeButtonText: String = appString(R.string.biometric_cancel),
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
        onCancel: () -> Unit = {}
    ) {
        showAuthPrompt(
            activity = activity,
            title = title,
            subtitle = subtitle,
            onSuccess = onSuccess,
            onError = onError,
            onCancel = onCancel
        )
    }

    enum class AuthStatus {
        AVAILABLE,       // 可用（生物识别或设备凭据）
        NO_HARDWARE,    // 没有硬件
        HARDWARE_UNAVAILABLE, // 硬件不可用
        NOT_ENROLLED,   // 未录入任何凭据
        UNKNOWN         // 未知
    }

    enum class BiometricStatus {
        AVAILABLE,       // 生物识别可用
        NO_HARDWARE,    // 没有生物识别硬件
        HARDWARE_UNAVAILABLE, // 硬件不可用
        NOT_ENROLLED,   // 未录入生物识别
        UNKNOWN         // 未知
    }
}
