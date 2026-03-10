package wang.zengye.dsm.ui.feedback

import android.content.Intent
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import wang.zengye.dsm.R

private enum class FeedbackType { BUG, FEATURE, OTHER }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf(FeedbackType.BUG) }
    var subject by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var includeDeviceInfo by remember { mutableStateOf(true) }
    var showSuccess by remember { mutableStateOf(false) }
    var subjectError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val successMessage = stringResource(R.string.feedback_submit_success)

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feedback_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 反馈类型选择
            Text(
                text = stringResource(R.string.feedback_type),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedbackType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = { selectedType = type },
                        label = {
                            Text(
                                when (type) {
                                    FeedbackType.BUG -> stringResource(R.string.feedback_type_bug)
                                    FeedbackType.FEATURE -> stringResource(R.string.feedback_type_feature)
                                    FeedbackType.OTHER -> stringResource(R.string.feedback_type_other)
                                }
                            )
                        },
                        leadingIcon = if (selectedType == type) {
                            { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            // 标题输入
            OutlinedTextField(
                value = subject,
                onValueChange = { subject = it; subjectError = false },
                label = { Text(stringResource(R.string.feedback_subject)) },
                placeholder = { Text(stringResource(R.string.feedback_subject_hint)) },
                isError = subjectError,
                supportingText = if (subjectError) {
                    { Text(stringResource(R.string.feedback_subject_required)) }
                } else null,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            // 详细描述
            OutlinedTextField(
                value = content,
                onValueChange = { content = it; contentError = false },
                label = { Text(stringResource(R.string.feedback_content)) },
                placeholder = { Text(stringResource(R.string.feedback_content_hint)) },
                isError = contentError,
                supportingText = if (contentError) {
                    { Text(stringResource(R.string.feedback_content_required)) }
                } else null,
                minLines = 5,
                maxLines = 10,
                modifier = Modifier.fillMaxWidth()
            )

            // 设备信息开关
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.feedback_device_info),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = stringResource(R.string.feedback_auto_collect),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = includeDeviceInfo,
                        onCheckedChange = { includeDeviceInfo = it }
                    )
                }
            }

            // 提交按钮
            Button(
                onClick = {
                    subjectError = subject.isBlank()
                    contentError = content.isBlank()
                    if (!subjectError && !contentError) {
                        val typeLabel = when (selectedType) {
                            FeedbackType.BUG -> "[Bug]"
                            FeedbackType.FEATURE -> "[Feature]"
                            FeedbackType.OTHER -> "[Other]"
                        }
                        val body = buildString {
                            appendLine(content)
                            if (includeDeviceInfo) {
                                appendLine()
                                appendLine("--- Device Info ---")
                                appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}")
                                appendLine("Android: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                            }
                        }
                        val emailIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "message/rfc822"
                            putExtra(Intent.EXTRA_SUBJECT, "$typeLabel $subject")
                            putExtra(Intent.EXTRA_TEXT, body)
                        }
                        context.startActivity(Intent.createChooser(emailIntent, context.getString(R.string.feedback_send_email)))
                        showSuccess = true
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Send, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.feedback_submit))
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    LaunchedEffect(showSuccess) {
        if (showSuccess) {
            snackbarHostState.showSnackbar(successMessage)
            showSuccess = false
        }
    }
}
