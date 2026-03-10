package wang.zengye.dsm.ui.file

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.*

/**
 * 文件分享链接信息
 */
data class FileShareLink(
    val id: String,
    val name: String,
    val path: String,
    val url: String,
    val qrcode: String? = null,
    val dateExpired: String? = null,
    val dateAvailable: String? = null,
    val expireTimes: Int = 0
)

/**
 * 文件分享对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileShareDialog(
    filePath: String,
    shareLink: FileShareLink?,
    isLoading: Boolean = false,
    isSaving: Boolean = false,
    onDismiss: () -> Unit,
    onSave: (dateExpired: Long?, dateAvailable: Long?, expireTimes: Int) -> Unit,
    onDelete: () -> Unit
) {
    val context = LocalContext.current
    var expireTimes by remember { mutableStateOf(shareLink?.expireTimes?.toString() ?: "") }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    // 生成二维码
    val qrBitmap = remember(shareLink?.url) {
        if (shareLink?.url != null) {
            generateQRCode(shareLink.url)
        } else null
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.file_share_cancel_title)) },
            text = { Text(stringResource(R.string.file_share_cancel_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.file_share_cancel_title))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.file_share_back))
                }
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 标题栏
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.file_share_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_close))
                    }
                }

                HorizontalDivider()

                if (isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (shareLink != null) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState())
                            .padding(Spacing.Medium),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 二维码
                        if (qrBitmap != null) {
                            Surface(
                                modifier = Modifier.size(180.dp),
                                shape = MaterialTheme.shapes.medium,
                                color = MaterialTheme.colorScheme.surfaceContainerHighest
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        bitmap = qrBitmap.asImageBitmap(),
                                        contentDescription = stringResource(R.string.file_share_qrcode),
                                        modifier = Modifier.size(160.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(Spacing.Medium))

                        // 文件名
                        Text(
                            text = shareLink.name,
                            style = MaterialTheme.typography.titleMedium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = shareLink.path,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(Spacing.Medium))

                        // 分享链接
                        OutlinedTextField(
                            value = shareLink.url,
                            onValueChange = {},
                            label = { Text(stringResource(R.string.file_share_link_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = {
                                    copyToClipboard(context, shareLink.url)
                                }) {
                                    Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.file_share_copy_link))
                                }
                            },
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(Spacing.Medium))

                        // 访问次数限制
                        OutlinedTextField(
                            value = expireTimes,
                            onValueChange = { 
                                if (it.isEmpty() || it.all { c -> c.isDigit() }) {
                                    expireTimes = it
                                }
                            },
                            label = { Text(stringResource(R.string.file_share_times_label)) },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(Spacing.Medium))

                        // 提示信息
                        if (shareLink.dateExpired != null) {
                            Text(
                                text = stringResource(R.string.file_share_expired_time, shareLink.dateExpired),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.file_share_create_failed),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                HorizontalDivider()

                // 底部按钮
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Medium),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Medium)
                ) {
                    if (shareLink != null) {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            )
                        ) {
                            Text(stringResource(R.string.file_share_cancel_title))
                        }

                        Button(
                            onClick = {
                                val times = expireTimes.toIntOrNull() ?: 0
                                onSave(null, null, times)
                            },
                            modifier = Modifier.weight(1f),
                            enabled = !isSaving
                        ) {
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(stringResource(R.string.common_save))
                            }
                        }
                    } else {
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(stringResource(R.string.common_close))
                        }
                    }
                }
            }
        }
    }
}

/**
 * 生成二维码
 */
private fun generateQRCode(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bitmap.setPixel(
                    x, y,
                    if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
                )
            }
        }
        bitmap
    } catch (e: Exception) {
        null
    }
}

/**
 * 复制到剪贴板
 */
private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("share_link", text)
    clipboardManager.setPrimaryClip(clip)
}