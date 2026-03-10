package wang.zengye.dsm.ui.logcenter

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogCenterScreen(
    onBack: () -> Unit = {}
) {
    val viewModel: LogCenterViewModel = hiltViewModel()
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf(
        stringResource(R.string.log_recent_tab),
        stringResource(R.string.log_logs_tab),
        stringResource(R.string.log_history_tab),
        stringResource(R.string.log_statistics_tab)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.log_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(LogCenterIntent.Refresh) }) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (index == 1) {
                                viewModel.sendIntent(LogCenterIntent.LoadLogs(0))
                            } else if (index == 2) {
                                viewModel.sendIntent(LogCenterIntent.LoadHistories)
                            }
                        },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> RecentLogsTab(uiState)
                1 -> LogsTab(uiState, viewModel)
                2 -> HistoryTab(uiState)
                3 -> StatisticsTab(uiState)
            }
        }
    }
}
