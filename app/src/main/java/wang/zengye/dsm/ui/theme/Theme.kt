package wang.zengye.dsm.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import wang.zengye.dsm.util.SettingsManager

// ==================== 浅色主题颜色 ====================
// 主色调
private val LightPrimary = Color(0xFF1976D2)
private val LightOnPrimary = Color.White
private val LightPrimaryContainer = Color(0xFFD1E4FF)
private val LightOnPrimaryContainer = Color(0xFF001D36)

// 次要色调
private val LightSecondary = Color(0xFF535F70)
private val LightOnSecondary = Color.White
private val LightSecondaryContainer = Color(0xFFD7E3F7)
private val LightOnSecondaryContainer = Color(0xFF101C2B)

// 第三色调
private val LightTertiary = Color(0xFF6B5778)
private val LightOnTertiary = Color.White
private val LightTertiaryContainer = Color(0xFFF2DAFF)
private val LightOnTertiaryContainer = Color(0xFF251431)

// 背景色
private val LightBackground = Color(0xFFF6F6F9)
private val LightOnBackground = Color(0xFF1A1C1E)

// 表面色 - MD3多层级表面系统
private val LightSurface = Color(0xFFF6F6F9)
private val LightOnSurface = Color(0xFF1A1C1E)
private val LightSurfaceVariant = Color(0xFFDFE2EB)
private val LightOnSurfaceVariant = Color(0xFF43474E)
private val LightSurfaceTint = Color(0xFF1976D2)

// 表面容器 - 用于卡片和弹窗
private val LightSurfaceContainerLowest = Color(0xFFFFFFFF)
private val LightSurfaceContainerLow = Color(0xFFFAFAFC)
private val LightSurfaceContainer = Color(0xFFF4F4F7)
private val LightSurfaceContainerHigh = Color(0xFFEDEDF0)
private val LightSurfaceContainerHighest = Color(0xFFE7E7EA)

// 反转表面
private val LightInverseSurface = Color(0xFF2F3033)
private val LightInverseOnSurface = Color(0xFFF1F0F4)
private val LightInversePrimary = Color(0xFF9ECAFF)

// 轮廓
private val LightOutline = Color(0xFF73777F)
private val LightOutlineVariant = Color(0xFFC3C6CF)

// 错误色
private val LightError = Color(0xFFBA1A1A)
private val LightOnError = Color.White
private val LightErrorContainer = Color(0xFFFFDAD6)
private val LightOnErrorContainer = Color(0xFF410002)

// 成功色
private val LightSuccess = Color(0xFF2E7D32)
private val LightOnSuccess = Color.White
private val LightSuccessContainer = Color(0xFFC8E6C9)
private val LightOnSuccessContainer = Color(0xFF1B5E20)

// 警告色
private val LightWarning = Color(0xFFED6C02)
private val LightOnWarning = Color.White
private val LightWarningContainer = Color(0xFFFFF3E0)
private val LightOnWarningContainer = Color(0xFFE65100)

// 信息色
private val LightInfo = Color(0xFF0288D1)
private val LightOnInfo = Color.White
private val LightInfoContainer = Color(0xFFE1F5FE)
private val LightOnInfoContainer = Color(0xFF01579B)

// ==================== 深色主题颜色 ====================
// 主色调
private val DarkPrimary = Color(0xFF9ECAFF)
private val DarkOnPrimary = Color(0xFF003258)
private val DarkPrimaryContainer = Color(0xFF00497D)
private val DarkOnPrimaryContainer = Color(0xFFD1E4FF)

// 次要色调
private val DarkSecondary = Color(0xFFBBC7DB)
private val DarkOnSecondary = Color(0xFF253140)
private val DarkSecondaryContainer = Color(0xFF3B4858)
private val DarkOnSecondaryContainer = Color(0xFFD7E3F7)

// 第三色调
private val DarkTertiary = Color(0xFFD6BEE4)
private val DarkOnTertiary = Color(0xFF3B2948)
private val DarkTertiaryContainer = Color(0xFF52405F)
private val DarkOnTertiaryContainer = Color(0xFFF2DAFF)

// 背景色
private val DarkBackground = Color(0xFF111318)
private val DarkOnBackground = Color(0xFFE3E2E6)

// 表面色 - MD3多层级表面系统
private val DarkSurface = Color(0xFF111318)
private val DarkOnSurface = Color(0xFFE3E2E6)
private val DarkSurfaceVariant = Color(0xFF43474E)
private val DarkOnSurfaceVariant = Color(0xFFC3C6CF)
private val DarkSurfaceTint = Color(0xFF9ECAFF)

// 表面容器 - 用于卡片和弹窗
private val DarkSurfaceContainerLowest = Color(0xFF0C0E13)
private val DarkSurfaceContainerLow = Color(0xFF161920)
private val DarkSurfaceContainer = Color(0xFF1B1E24)
private val DarkSurfaceContainerHigh = Color(0xFF1C1F25)
private val DarkSurfaceContainerHighest = Color(0xFF272A31)

// 反转表面
private val DarkInverseSurface = Color(0xFFE3E2E6)
private val DarkInverseOnSurface = Color(0xFF2F3033)
private val DarkInversePrimary = Color(0xFF1976D2)

// 轮廓
private val DarkOutline = Color(0xFF8D9199)
private val DarkOutlineVariant = Color(0xFF43474E)

// 错误色
private val DarkError = Color(0xFFFFB4AB)
private val DarkOnError = Color(0xFF690005)
private val DarkErrorContainer = Color(0xFF93000A)
private val DarkOnErrorContainer = Color(0xFFFFDAD6)

// 成功色
private val DarkSuccess = Color(0xFF81C784)
private val DarkOnSuccess = Color(0xFF003300)
private val DarkSuccessContainer = Color(0xFF1B5E20)
private val DarkOnSuccessContainer = Color(0xFFC8E6C9)

// 警告色
private val DarkWarning = Color(0xFFFFB74D)
private val DarkOnWarning = Color(0xFF3E2723)
private val DarkWarningContainer = Color(0xFFE65100)
private val DarkOnWarningContainer = Color(0xFFFFF3E0)

// 信息色
private val DarkInfo = Color(0xFF4FC3F7)
private val DarkOnInfo = Color(0xFF0D47A1)
private val DarkInfoContainer = Color(0xFF01579B)
private val DarkOnInfoContainer = Color(0xFFE1F5FE)

// ==================== 自定义颜色 ====================
// 卡片表面色
val LightCardSurface = Color(0xFFFFFFFF)
val DarkCardSurface = Color(0xFF1C1F25)

// 标题颜色
val LightTitleColor = Color(0xFF1A1C1E)
val DarkTitleColor = Color(0xFFE3E2E6)

// 占位符颜色
val LightPlaceholderColor = Color(0xFF939393)
val DarkPlaceholderColor = Color(0xFF757575)

// 进度条颜色
val LightProgressColor = Color(0xFFE0E0E0)
val DarkProgressColor = Color(0xFF424242)

// 分割线颜色
val LightDividerColor = Color(0xFFE0E0E0)
val DarkDividerColor = Color(0xFF424242)

private val LightColorScheme = lightColorScheme(
    // 主色调
    primary = LightPrimary,
    onPrimary = LightOnPrimary,
    primaryContainer = LightPrimaryContainer,
    onPrimaryContainer = LightOnPrimaryContainer,
    // 次要色调
    secondary = LightSecondary,
    onSecondary = LightOnSecondary,
    secondaryContainer = LightSecondaryContainer,
    onSecondaryContainer = LightOnSecondaryContainer,
    // 第三色调
    tertiary = LightTertiary,
    onTertiary = LightOnTertiary,
    tertiaryContainer = LightTertiaryContainer,
    onTertiaryContainer = LightOnTertiaryContainer,
    // 背景和表面
    background = LightBackground,
    onBackground = LightOnBackground,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant,
    surfaceTint = LightSurfaceTint,
    // 表面容器层级
    surfaceContainerLowest = LightSurfaceContainerLowest,
    surfaceContainerLow = LightSurfaceContainerLow,
    surfaceContainer = LightSurfaceContainer,
    surfaceContainerHigh = LightSurfaceContainerHigh,
    surfaceContainerHighest = LightSurfaceContainerHighest,
    // 反转表面
    inverseSurface = LightInverseSurface,
    inverseOnSurface = LightInverseOnSurface,
    inversePrimary = LightInversePrimary,
    // 轮廓
    outline = LightOutline,
    outlineVariant = LightOutlineVariant,
    // 错误
    error = LightError,
    onError = LightOnError,
    errorContainer = LightErrorContainer,
    onErrorContainer = LightOnErrorContainer
)

private val DarkColorScheme = darkColorScheme(
    // 主色调
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    // 次要色调
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    // 第三色调
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    // 背景和表面
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    // 表面容器层级
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    // 反转表面
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    inversePrimary = DarkInversePrimary,
    // 轮廓
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    // 错误
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer
)

enum class DarkMode {
    LIGHT,      // 浅色
    DARK,       // 深色
    SYSTEM      // 跟随系统
}

@Composable
fun DSMTheme(
    darkMode: DarkMode = DarkMode.SYSTEM,
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val savedDarkMode by SettingsManager.darkMode.collectAsState(initial = DarkMode.SYSTEM)
    val effectiveDarkMode = if (darkMode == DarkMode.SYSTEM) savedDarkMode else darkMode
    
    val darkTheme = when (effectiveDarkMode) {
        DarkMode.LIGHT -> false
        DarkMode.DARK -> true
        DarkMode.SYSTEM -> isSystemInDarkTheme()
    }
    
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }
    
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// ==================== 扩展属性获取自定义颜色 ====================
// 卡片表面色
val MaterialTheme.cardSurface: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkCardSurface else LightCardSurface

// 主色渐变 - 根据主题返回不同颜色
@Composable
fun primaryGradient(): Brush {
    val isDark = isSystemInDarkTheme()
    return Brush.horizontalGradient(
        colors = if (isDark) {
            listOf(
                Color(0xFF1565C0), // 深蓝色
                Color(0xFF6A4C93)  // 深紫色
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.tertiary
            )
        }
    )
}

val MaterialTheme.titleColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkTitleColor else LightTitleColor

val MaterialTheme.placeholderColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkPlaceholderColor else LightPlaceholderColor

val MaterialTheme.progressColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkProgressColor else LightProgressColor

val MaterialTheme.dividerColor: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkDividerColor else LightDividerColor

// 成功色
val MaterialTheme.success: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkSuccess else LightSuccess

val MaterialTheme.onSuccess: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnSuccess else LightOnSuccess

val MaterialTheme.successContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkSuccessContainer else LightSuccessContainer

val MaterialTheme.onSuccessContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnSuccessContainer else LightOnSuccessContainer

// 警告色
val MaterialTheme.warning: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkWarning else LightWarning

val MaterialTheme.onWarning: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnWarning else LightOnWarning

val MaterialTheme.warningContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkWarningContainer else LightWarningContainer

val MaterialTheme.onWarningContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnWarningContainer else LightOnWarningContainer

// 信息色
val MaterialTheme.info: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkInfo else LightInfo

val MaterialTheme.onInfo: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnInfo else LightOnInfo

val MaterialTheme.infoContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkInfoContainer else LightInfoContainer

val MaterialTheme.onInfoContainer: Color
    @Composable
    get() = if (isSystemInDarkTheme()) DarkOnInfoContainer else LightOnInfoContainer

// ==================== 辅助函数 ====================
/**
 * 根据使用率获取对应颜色
 */
@Composable
fun getUsageColor(usage: Int): Color {
    return when {
        usage > 90 -> MaterialTheme.colorScheme.error
        usage > 70 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * 根据进度获取对应颜色
 */
@Composable
fun getProgressColor(progress: Float): Color {
    return when {
        progress > 0.9f -> MaterialTheme.colorScheme.error
        progress > 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

/**
 * 根据状态获取状态颜色
 */
@Composable
fun getStatusColor(status: String): Color {
    return when (status.lowercase()) {
        "normal", "running", "online", "healthy" -> MaterialTheme.colorScheme.primary
        "warning", "degrade" -> MaterialTheme.warning
        "error", "critical", "offline" -> MaterialTheme.colorScheme.error
        "success", "completed" -> MaterialTheme.success
        "info", "pending" -> MaterialTheme.info
        else -> MaterialTheme.colorScheme.outline
    }
}
