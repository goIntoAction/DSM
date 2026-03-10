package wang.zengye.dsm.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material Design 3 设计系统
 * 统一定义间距、圆角、阴影等设计规范
 */

/**
 * 间距系统 - 基于8dp网格
 */
object Spacing {
    /** 无间距 */
    val None: Dp = 0.dp
    
    /** 极小间距 - 2dp */
    val ExtraSmall: Dp = 2.dp
    
    /** 小间距 - 4dp */
    val Small: Dp = 4.dp
    
    /** 中小间距 - 8dp */
    val MediumSmall: Dp = 8.dp
    
    /** 中间距 - 12dp */
    val Medium: Dp = 12.dp
    
    /** 标准间距 - 16dp (默认内容边距) */
    val Standard: Dp = 16.dp
    
    /** 中大间距 - 20dp */
    val MediumLarge: Dp = 20.dp
    
    /** 大间距 - 24dp */
    val Large: Dp = 24.dp
    
    /** 较大间距 - 32dp */
    val ExtraLarge: Dp = 32.dp
    
    /** 超大间距 - 48dp */
    val Huge: Dp = 48.dp
    
    /** 页面水平边距 */
    val PageHorizontal: Dp = 20.dp

    /** 页面屏幕边距 */
    val ScreenPadding: Dp = 20.dp
    
    /** 卡片内边距 */
    val CardPadding: Dp = 16.dp
    
    /** 列表项内边距 */
    val ListItemPadding: Dp = 16.dp
    
    /** 元素间距 */
    val ItemSpacing: Dp = 8.dp
    
    /** 卡片间距 */
    val CardSpacing: Dp = 16.dp
    
    /** 分组间距 */
    val SectionSpacing: Dp = 16.dp
}

/**
 * 圆角系统 - MD3风格更圆润
 */
object CornerRadius {
    /** 无圆角 */
    val None: Dp = 0.dp
    
    /** 极小圆角 - 4dp */
    val ExtraSmall: Dp = 4.dp
    
    /** 小圆角 - 8dp */
    val Small: Dp = 8.dp
    
    /** 中圆角 - 12dp (默认卡片圆角) */
    val Medium: Dp = 12.dp
    
    /** 大圆角 - 16dp */
    val Large: Dp = 16.dp
    
    /** 较大圆角 - 20dp */
    val ExtraLarge: Dp = 20.dp
    
    /** 超大圆角 - 28dp (FAB、对话框) */
    val Huge: Dp = 28.dp
    
    /** 完全圆形 */
    val Full: Dp = 9999.dp
    
    /** 按钮圆角 */
    val Button: Dp = 20.dp
    
    /** 卡片圆角 */
    val Card: Dp = 20.dp
    
    /** 对话框圆角 */
    val Dialog: Dp = 28.dp
    
    /** 输入框圆角 */
    val TextField: Dp = 4.dp
}

/**
 * 阴影/高度系统
 */
object Elevation {
    /** 无阴影 */
    val None: Dp = 0.dp
    
    /** 低阴影 - 1dp */
    val Low: Dp = 1.dp
    
    /** 中低阴影 - 2dp */
    val MediumLow: Dp = 2.dp
    
    /** 中阴影 - 3dp */
    val Medium: Dp = 3.dp
    
    /** 中高阴影 - 4dp */
    val MediumHigh: Dp = 4.dp
    
    /** 高阴影 - 6dp */
    val High: Dp = 6.dp
    
    /** 较高阴影 - 8dp */
    val ExtraHigh: Dp = 8.dp
    
    /** 卡片阴影 */
    val Card: Dp = 1.dp
    
    /** Elevated卡片阴影 */
    val CardElevated: Dp = 3.dp
    
    /** 对话框阴影 */
    val Dialog: Dp = 6.dp
    
    /** 导航栏阴影 */
    val NavigationBar: Dp = 3.dp
    
    /** FAB阴影 */
    val Fab: Dp = 6.dp
}

/**
 * 尺寸系统
 */
object Dimensions {
    /** 图标尺寸 - 小 */
    val IconSmall: Dp = 16.dp
    
    /** 图标尺寸 - 标准 */
    val IconStandard: Dp = 24.dp
    
    /** 图标尺寸 - 大 */
    val IconLarge: Dp = 32.dp
    
    /** 图标尺寸 - 超大 */
    val IconExtraLarge: Dp = 48.dp
    
    /** 图标按钮尺寸 */
    val IconButton: Dp = 48.dp
    
    /** 列表项高度 */
    val ListItemHeight: Dp = 56.dp
    
    /** 列表项高度 - 紧凑 */
    val ListItemHeightCompact: Dp = 48.dp
    
    /** 头像尺寸 - 小 */
    val AvatarSmall: Dp = 32.dp
    
    /** 头像尺寸 - 标准 */
    val AvatarStandard: Dp = 40.dp
    
    /** 头像尺寸 - 大 */
    val AvatarLarge: Dp = 56.dp
    
    /** 进度条高度 */
    val ProgressBarHeight: Dp = 4.dp
    
    /** 进度条高度 - 粗 */
    val ProgressBarHeightThick: Dp = 8.dp
    
    /** 分割线粗细 */
    val DividerThickness: Dp = 1.dp
    
    /** 卡片最小高度 */
    val CardMinHeight: Dp = 80.dp
    
    /** 图表高度 */
    val ChartHeight: Dp = 120.dp
    
    /** 圆形进度条尺寸 */
    val CircularProgressIndicator: Dp = 48.dp
    
    /** 圆形进度条尺寸 - 小 */
    val CircularProgressIndicatorSmall: Dp = 24.dp
}

/**
 * 动画时长
 */
object AnimationDuration {
    /** 快速动画 - 150ms */
    const val Fast: Int = 150
    
    /** 标准动画 - 300ms */
    const val Standard: Int = 300
    
    /** 慢速动画 - 500ms */
    const val Slow: Int = 500
    
    /** 页面切换动画 */
    const val PageTransition: Int = 300
    
    /** 列表项动画 */
    const val ListItem: Int = 200
}

/**
 * 预定义形状
 */
object AppShapes {
    /** 小圆角形状 */
    val Small = RoundedCornerShape(CornerRadius.Small)
    
    /** 中圆角形状 */
    val Medium = RoundedCornerShape(CornerRadius.Medium)
    
    /** 大圆角形状 */
    val Large = RoundedCornerShape(CornerRadius.Large)
    
    /** 超大圆角形状 */
    val ExtraLarge = RoundedCornerShape(CornerRadius.ExtraLarge)
    
    /** 卡片形状 */
    val Card = RoundedCornerShape(CornerRadius.Card)
    
    /** 按钮形状 */
    val Button = RoundedCornerShape(CornerRadius.Button)
    
    /** 对话框形状 */
    val Dialog = RoundedCornerShape(CornerRadius.Dialog)
    
    /** 完全圆形 */
    val Full = RoundedCornerShape(CornerRadius.Full)
}

/**
 * 扩展属性 - 便于在Composable中访问设计系统
 */
val MaterialTheme.spacing: Spacing
    @Composable
    @ReadOnlyComposable
    get() = Spacing

val MaterialTheme.cornerRadius: CornerRadius
    @Composable
    @ReadOnlyComposable
    get() = CornerRadius

val MaterialTheme.elevation: Elevation
    @Composable
    @ReadOnlyComposable
    get() = Elevation

val MaterialTheme.dimensions: Dimensions
    @Composable
    @ReadOnlyComposable
    get() = Dimensions

val MaterialTheme.appShapes: AppShapes
    @Composable
    @ReadOnlyComposable
    get() = AppShapes
