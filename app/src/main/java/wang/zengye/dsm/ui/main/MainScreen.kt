package wang.zengye.dsm.ui.main

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import wang.zengye.dsm.navigation.*
import wang.zengye.dsm.ui.theme.DSMTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onLogout: () -> Unit
) {
    DSMTheme {
        val navController = rememberNavController()
        val navBackStackEntry by navController.currentBackStackEntryAsState()

        // 判断是否显示底部导航栏
        // 检查当��路由或其父路由是否是底部导航项
        val showBottomBar = navBackStackEntry?.destination?.route?.let { currentRoute ->
            BottomNavItem.all.any { item ->
                // 检查是否匹配或当前路由是该tab下的子路由
                currentRoute.contains(item.route::class.simpleName ?: "") ||
                navBackStackEntry?.destination?.hasRoute(item.route::class) == true
            }
        } == true

        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            bottomBar = {
                if (showBottomBar) {
                    MainBottomNavBar(
                        currentEntry = navBackStackEntry,
                        onNavigate = { route ->
                            // 检查当前路由
                            val currentRoute = navBackStackEntry?.destination?.route?.toString()
                            val targetRouteString = route.toString()

                            // 如果已经在目标路由上，不做任何操作
                            if (currentRoute == targetRouteString) {
                                return@MainBottomNavBar
                            }

                            // 尝试 popBackStack 到目标路由（保持状态）
                            val popped = navController.popBackStack(route, inclusive = false)
                            if (!popped) {
                                // 目标路由不在栈中，正常导航
                                navController.navigate(route) {
                                    launchSingleTop = true
                                }
                            }
                        }
                    )
                }
            }
        ) { paddingValues ->
            MainNavHost(
                navController = navController,
                modifier = Modifier.padding(paddingValues),
                onLogout = onLogout
            )
        }
    }
}
@Composable
private fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = DsmRoute.Dashboard,
        modifier = modifier,
        // 进入页面：从右侧滑入
        enterTransition = { slideInFromRight },
        // 离开页面：保持原地淡出（让新页面覆盖）
        exitTransition = { fadeOut(animationSpec = tween(NAV_ANIM_DURATION / 2)) },
        // 返回时，前页面从左侧轻微滑入
        popEnterTransition = { slideInFromLeft },
        // 返回时，当前页面向右滑出
        popExitTransition = { slideOutToRight }
    ) {
        dashboardNavGraph(navController)
        fileNavGraph(navController)
        downloadNavGraph(navController)
        photosNavGraph(navController)
        dockerNavGraph(navController)
        controlPanelNavGraph(navController)
        settingNavGraph(navController, onLogout)
        playerNavGraph(navController)
        systemNavGraph(navController)
    }
}
