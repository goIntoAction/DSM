package wang.zengye.dsm.ui.player.controls

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.ScreenLockRotation
import androidx.compose.material.icons.filled.ScreenRotation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

/**
 * 左下角控制栏
 * 锁定、旋转、方向锁定、播放速度按钮
 */
@Composable
fun BottomLeftPlayerControls(
    playbackSpeed: Float,
    isOrientationLocked: Boolean,
    onLockControls: () -> Unit,
    onCycleRotation: () -> Unit,
    onToggleOrientationLock: () -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onOpenSpeedSheet: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ControlsButton(
            Icons.Default.LockOpen,
            onClick = onLockControls,
        )
        ControlsButton(
            icon = Icons.Default.AspectRatio,
            onClick = onCycleRotation,
        )
        ControlsButton(
            icon = if (isOrientationLocked) Icons.Default.ScreenLockRotation else Icons.Default.ScreenRotation,
            onClick = onToggleOrientationLock,
        )
        ControlsButton(
            text = String.format("%.2fx", playbackSpeed),
            onClick = {
                onPlaybackSpeedChange(if (playbackSpeed >= 4f) 0.25f else (playbackSpeed + 0.25f))
            },
            onLongClick = onOpenSpeedSheet,
        )
    }
}
