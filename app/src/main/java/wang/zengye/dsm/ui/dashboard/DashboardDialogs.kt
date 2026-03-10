package wang.zengye.dsm.ui.dashboard

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PowerSettingsNew
import androidx.compose.material.icons.outlined.RestartAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.AppShapes

/**
 * 电源操作确认对话框 - MD3风格
 */
@Composable
internal fun PowerConfirmDialog(
    powerAction: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = if (powerAction == "shutdown") Icons.Outlined.PowerSettingsNew else Icons.Outlined.RestartAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error
            )
        },
        title = {
            Text(
                text = if (powerAction == "shutdown") stringResource(R.string.dashboard_confirm_shutdown) else stringResource(R.string.dashboard_confirm_reboot),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Medium
            )
        },
        text = {
            Text(
                text = if (powerAction == "shutdown")
                    stringResource(R.string.dashboard_shutdown_message)
                else
                    stringResource(R.string.dashboard_reboot_message),
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            FilledTonalButton(
                onClick = onConfirm,
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                )
            ) {
                Text(stringResource(R.string.common_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        },
        shape = AppShapes.Dialog
    )
}
