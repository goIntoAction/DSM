package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import wang.zengye.dsm.ui.setting.SettingScreen

fun NavGraphBuilder.settingNavGraph(
    navController: NavHostController,
    onLogout: () -> Unit
) {
    // 设置
    composable<DsmRoute.Setting>(
        enterTransition = { tabEnterTransition },
        exitTransition = { tabExitTransition },
        popEnterTransition = { tabEnterTransition },
        popExitTransition = { tabExitTransition }
    ) {
        SettingScreen(
            onNavigateToAbout = { navController.navigate(DsmRoute.About) },
            onNavigateToFeedback = { navController.navigate(DsmRoute.Feedback) },
            onNavigateToPreferences = { navController.navigate(DsmRoute.Preferences) },
            onNavigateToDocker = { navController.navigate(DsmRoute.Docker) },
            onNavigateToPackages = { navController.navigate(DsmRoute.Packages) },
            onNavigateToVirtualMachine = { navController.navigate(DsmRoute.VirtualMachine) },
            onNavigateToSmartTest = { navController.navigate(DsmRoute.SmartTest()) },
            onNavigateToBackup = { navController.navigate(DsmRoute.Backup) },
            onNavigateToOpenSource = { navController.navigate(DsmRoute.OpenSource) },
            onLogout = onLogout
        )
    }

    // 关于
    composable<DsmRoute.About> {
        wang.zengye.dsm.ui.about.AboutScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 反馈
    composable<DsmRoute.Feedback> {
        wang.zengye.dsm.ui.feedback.FeedbackScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 首选项/设置
    composable<DsmRoute.Preferences> {
        wang.zengye.dsm.ui.settings.PreferencesScreen(
            onBack = { navController.popBackStack() }
        )
    }

    // 备份管理
    composable<DsmRoute.Backup> {
        wang.zengye.dsm.ui.backup.BackupScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 手势密码
    composable<DsmRoute.GesturePassword> {
        wang.zengye.dsm.ui.security.GesturePasswordScreen(
            onNavigateBack = { navController.popBackStack() },
            onVerifySuccess = { navController.popBackStack() }
        )
    }

    // OTP 绑定
    composable<DsmRoute.OtpBind> {
        wang.zengye.dsm.ui.setting.OtpBindScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // 开源协议
    composable<DsmRoute.OpenSource> {
        wang.zengye.dsm.ui.setting.OpenSourceScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
