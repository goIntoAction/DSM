package wang.zengye.dsm.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*

/** 动画时长常量 */
const val NAV_ANIM_DURATION = 350
const val TAB_ANIM_DURATION = 200

/**
 * 底部 Tab 页面的切换动画
 * 使用淡入淡出 + 轻微缩放，创造层次感
 */
val tabEnterTransition: EnterTransition
    get() = fadeIn(
        animationSpec = tween(TAB_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.98f,
        animationSpec = tween(TAB_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

val tabExitTransition: ExitTransition
    get() = fadeOut(
        animationSpec = tween(TAB_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.98f,
        animationSpec = tween(TAB_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 普通页面进入动画 - 从右侧滑入
 */
val slideInFromRight: EnterTransition
    get() = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 普通页面退出动画 - 向右滑出（返回时）
 */
val slideOutToRight: ExitTransition
    get() = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 页面返回时的进入动画 - 从左侧滑入
 */
val slideInFromLeft: EnterTransition
    get() = slideInHorizontally(
        initialOffsetX = { -it / 3 },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 页面返回时的退出动画 - 向左滑出
 */
val slideOutToLeft: ExitTransition
    get() = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 模态页面进入动画 - 从底部滑入
 * 适用于全屏对话框、图片查看器等
 */
val slideInFromBottom: EnterTransition
    get() = slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeIn(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 模态页面退出动画 - 向底部滑出
 */
val slideOutToBottom: ExitTransition
    get() = slideOutVertically(
        targetOffsetY = { it },
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + fadeOut(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

/**
 * 共享元素过渡动画 - 带缩放的淡入淡出
 * 适用于详情页等需要视觉连贯的场景
 */
val sharedElementEnter: EnterTransition
    get() = fadeIn(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + scaleIn(
        initialScale = 0.92f,
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )

val sharedElementExit: ExitTransition
    get() = fadeOut(
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    ) + scaleOut(
        targetScale = 0.92f,
        animationSpec = tween(NAV_ANIM_DURATION, easing = FastOutSlowInEasing)
    )