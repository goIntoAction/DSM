package wang.zengye.dsm.ui.player.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.outlined.PictureInPicture
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * 右下角控制栏
 * 与 mpvKt 一致：上一个/下一个、画中画、画面比例按钮
 */
@Composable
fun BottomRightPlayerControls(
    hasPrevious: Boolean,
    hasNext: Boolean,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    isPipAvailable: Boolean,
    onPipClick: () -> Unit,
    onAspectClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 上一个
        ControlsButton(
            Icons.Default.SkipPrevious,
            onClick = onPreviousClick,
            color = if (hasPrevious) Color.White else Color.White.copy(alpha = 0.3f),
        )
        // 下一个
        ControlsButton(
            Icons.Default.SkipNext,
            onClick = onNextClick,
            color = if (hasNext) Color.White else Color.White.copy(alpha = 0.3f),
        )
        // 画中画
        if (isPipAvailable) {
            ControlsButton(
                Icons.Outlined.PictureInPicture,
                onClick = onPipClick,
            )
        }
        // 画面比例
        ControlsButton(
            Icons.Default.FitScreen,
            onClick = onAspectClick,
        )
    }
}