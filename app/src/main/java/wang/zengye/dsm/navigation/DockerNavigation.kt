package wang.zengye.dsm.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import wang.zengye.dsm.ui.docker.DockerScreen

fun NavGraphBuilder.dockerNavGraph(navController: NavHostController) {
    // Docker
    composable<DsmRoute.Docker> {
        DockerScreen(
            onNavigateToImages = {
                navController.navigate(DsmRoute.DockerImages)
            },
            onNavigateToNetworks = {
                navController.navigate(DsmRoute.DockerNetworks)
            },
            onNavigateToContainerDetail = { containerName ->
                navController.navigate(DsmRoute.DockerContainerDetail(name = containerName))
            }
        )
    }

    // Docker 容器详情
    composable<DsmRoute.DockerContainerDetail> { backStackEntry ->
        val route = backStackEntry.toRoute<DsmRoute.DockerContainerDetail>()
        wang.zengye.dsm.ui.docker.DockerContainerDetailScreen(
            containerName = route.name,
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Docker 镜像
    composable<DsmRoute.DockerImages> {
        wang.zengye.dsm.ui.docker.ImageListScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }

    // Docker 网络
    composable<DsmRoute.DockerNetworks> {
        wang.zengye.dsm.ui.docker.NetworkListScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
