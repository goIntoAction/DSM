package wang.zengye.dsm.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hasRoute
import wang.zengye.dsm.navigation.BottomNavItem

/**
 * MD3风格底部导航栏
 */
@Composable
internal fun MainBottomNavBar(
    currentEntry: NavBackStackEntry?,
    onNavigate: (Any) -> Unit
) {
    // MD3风格导航栏 - 无阴影，使用表面容器颜色
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 0.dp,
    ) {
        BottomNavItem.all.forEach { item ->
            val selected = currentEntry?.destination?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = if (selected) item.icon else getOutlinedIcon(item.icon),
                        contentDescription = stringResource(item.titleResId)
                    )
                },
                label = {
                    Text(
                        text = stringResource(item.titleResId),
                        fontWeight = if (selected) FontWeight.Medium else FontWeight.Normal
                    )
                }
            )
        }
    }
}

/**
 * 获取轮廓版本图标（未选中状态）
 */
private fun getOutlinedIcon(filledIcon: ImageVector): ImageVector {
    return when (filledIcon) {
        Icons.Filled.Dashboard -> Icons.Outlined.Dashboard
        Icons.Filled.Folder -> Icons.Outlined.Folder
        Icons.Filled.Photo -> Icons.Outlined.Photo
        Icons.Filled.Download -> Icons.Outlined.Download
        Icons.Filled.Settings -> Icons.Outlined.Settings
        else -> filledIcon
    }
}
