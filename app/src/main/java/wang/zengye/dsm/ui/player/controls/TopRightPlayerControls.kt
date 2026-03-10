package wang.zengye.dsm.ui.player.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 右上角控制栏
 * 与 mpvKt 一致：字幕、音轨、更多按钮
 */
@Composable
fun TopRightPlayerControls(
    onSubtitlesClick: () -> Unit,
    onAudioClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    onSubtitlesLongClick: () -> Unit = {},
    onAudioLongClick: () -> Unit = {},
    onMoreLongClick: () -> Unit = {},
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ControlsButton(
            Icons.Default.Subtitles,
            onClick = onSubtitlesClick,
            onLongClick = onSubtitlesLongClick,
        )
        ControlsButton(
            Icons.Default.Audiotrack,
            onClick = onAudioClick,
            onLongClick = onAudioLongClick,
        )
        ControlsButton(
            Icons.Default.MoreVert,
            onClick = onMoreClick,
            onLongClick = onMoreLongClick,
        )
    }
}