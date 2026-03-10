package wang.zengye.dsm.ui.player.controls.sheets

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import `is`.xyz.mpv.MPVLib
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.Spacing

/**
 * 更多选项面板
 * 复刻自 mpvKt
 */
@Composable
fun MoreSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var statisticsPage by remember { mutableIntStateOf(0) }

    PlayerSheet(
        onDismissRequest,
        modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.Standard),
            verticalArrangement = Arrangement.spacedBy(Spacing.Small),
        ) {
            // 标题行
            Text(
                text = stringResource(R.string.player_more_options),
                style = MaterialTheme.typography.headlineMedium,
            )

            // 统计页面
            Text(stringResource(R.string.player_stats_page))
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                repeat(6) { page ->
                    FilterChip(
                        label = { Text(if (page == 0) stringResource(R.string.player_stats_off) else stringResource(R.string.player_stats_page_n, page)) },
                        onClick = {
                            if ((page == 0) xor (statisticsPage == 0)) {
                                MPVLib.command("script-binding", "stats/display-stats-toggle")
                            }
                            if (page != 0) {
                                MPVLib.command("script-binding", "stats/display-page-$page")
                            }
                            statisticsPage = page
                        },
                        selected = statisticsPage == page,
                    )
                }
            }
        }
    }
}
