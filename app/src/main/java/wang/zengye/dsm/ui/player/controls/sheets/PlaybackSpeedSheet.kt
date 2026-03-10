package wang.zengye.dsm.ui.player.controls.sheets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.Spacing
import kotlin.math.pow
import kotlin.math.roundToInt

private const val PREFS_NAME = "player_prefs"
private const val KEY_DEFAULT_SPEED = "default_playback_speed"

fun Float.toFixed(precision: Int = 2): Float {
    val factor = 10.0f.pow(precision)
    return (this * factor).roundToInt() / factor
}

/**
 * 播放速度面板
 * 复刻自 mpvKt
 */
@Composable
fun PlaybackSpeedSheet(
    speed: Float,
    speedPresets: List<Float>,
    onSpeedChange: (Float) -> Unit,
    onAddSpeedPreset: (Float) -> Unit,
    onRemoveSpeedPreset: (Float) -> Unit,
    onResetPresets: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PlayerSheet(onDismissRequest = onDismissRequest) {
        Column(
            modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = Spacing.Standard),
        ) {
            // 速度滑块
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Standard, vertical = Spacing.Small),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Large),
            ) {
                Column(modifier = Modifier.weight(0.5f)) {
                    Text(
                        text = stringResource(R.string.player_playback_speed),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(String.format("%.2fx", speed))
                }

                val haptic = LocalHapticFeedback.current
                Slider(
                    value = speed,
                    onValueChange = {
                        if (it != speed) {
                            onSpeedChange(it)
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    modifier = Modifier.weight(1.5f),
                    valueRange = 0.25f..4f,
                )
            }

            // 预设速度
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spacing.Standard),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.Standard),
            ) {
                FilledTonalIconButton(onClick = onResetPresets) {
                    Icon(Icons.Default.RestartAlt, contentDescription = stringResource(R.string.player_reset))
                }
                LazyRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                ) {
                    items(speedPresets, key = { it }) { presetSpeed ->
                        InputChip(
                            selected = speed == presetSpeed,
                            onClick = { onSpeedChange(presetSpeed) },
                            label = { Text(String.format("%.2fx", presetSpeed)) },
                            modifier = Modifier.animateItem(),
                            trailingIcon = {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    modifier = Modifier.clickable { onRemoveSpeedPreset(presetSpeed.toFixed(2)) },
                                )
                            },
                        )
                    }
                }
                FilledTonalIconButton(onClick = { onAddSpeedPreset(speed.toFixed(2)) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.player_add_preset))
                }
            }

            // 默认速度按钮
            Row(
                modifier = Modifier
                    .padding(horizontal = Spacing.Standard)
                    .padding(top = Spacing.Standard),
                horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
            ) {
                val context = LocalContext.current
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        context.getSharedPreferences(PREFS_NAME, android.content.Context.MODE_PRIVATE)
                            .edit()
                            .putFloat(KEY_DEFAULT_SPEED, speed)
                            .apply()
                    },
                ) {
                    Text(text = stringResource(R.string.player_set_default_speed))
                }
                FilledIconButton(onClick = { onSpeedChange(1f) }) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = stringResource(R.string.player_reset))
                }
            }
        }
    }
}
