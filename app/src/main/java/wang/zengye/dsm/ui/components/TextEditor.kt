package wang.zengye.dsm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import java.io.File

/**
 * 文本编辑器组件
 * 支持查看和编辑文本文件
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextEditorScreen(
    filePath: String,
    initialContent: String = "",
    onBack: () -> Unit,
    onSave: ((String) -> Unit)? = null
) {
    var content by remember { mutableStateOf(initialContent) }
    var isEditing by remember { mutableStateOf(false) }
    var hasChanges by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // 检测文件类型
    val fileName = filePath.substringAfterLast("/")
    val fileExtension = fileName.substringAfterLast(".")
    val isCodeFile = listOf(
        "kt", "java", "py", "php", "html", "xml", "json",
        "js", "ts", "css", "scss", "sql", "sh", "md", "txt",
        "c", "cpp", "h", "hpp", "go", "rs", "swift", "yaml", "yml"
    ).contains(fileExtension.lowercase())

    // 加载文件内容
    LaunchedEffect(filePath) {
        if (initialContent.isEmpty() && filePath.isNotEmpty()) {
            isLoading = true
            try {
                // 判断是 URL 还是本地路径
                if (filePath.startsWith("http://") || filePath.startsWith("https://")) {
                    // 从 URL 加载
                    val connection = java.net.URL(filePath).openConnection()
                    connection.connectTimeout = 10000
                    connection.readTimeout = 30000
                    // 添加认证信息
                    val cookie = wang.zengye.dsm.data.api.DsmApiHelper.cookie
                    if (cookie.isNotEmpty()) {
                        connection.setRequestProperty("Cookie", cookie)
                    }
                    connection.getInputStream().use { input ->
                        content = input.bufferedReader().readText()
                    }
                } else {
                    // 如果是本地文件，读取内容
                    val file = File(filePath)
                    if (file.exists()) {
                        content = file.readText()
                    }
                }
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    // 监听内容变化
    LaunchedEffect(content) {
        hasChanges = content != initialContent
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = fileName.ifEmpty { stringResource(R.string.components_text_editor) },
                                maxLines = 1
                            )
                            if (hasChanges) {
                                Text(
                                    text = stringResource(R.string.components_unsaved),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (hasChanges) {
                                // TODO: 显示确认对话框
                            }
                            onBack()
                        }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.common_back)
                            )
                        }
                    },
                    actions = {
                        // 编辑/只读模式切换
                        if (onSave != null) {
                            IconButton(
                                onClick = { isEditing = !isEditing }
                            ) {
                                Icon(
                                    if (isEditing) Icons.Filled.Visibility else Icons.Filled.Edit,
                                    contentDescription = if (isEditing) stringResource(R.string.components_read_only_desc) else stringResource(R.string.components_edit_desc)
                                )
                            }
                            // 保存
                            IconButton(
                                onClick = {
                                    onSave(content)
                                    hasChanges = false
                                },
                                enabled = hasChanges
                            ) {
                                Icon(
                                    Icons.Filled.Save,
                                    contentDescription = stringResource(R.string.common_save)
                                )
                            }
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    errorMessage != null -> {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = stringResource(R.string.components_load_failed),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: stringResource(R.string.components_unknown_error),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    else -> {
                        if (isEditing) {
                            // 编辑模式 - 使用 TextField
                            BasicTextField(
                                value = content,
                                onValueChange = {
                                    content = it
                                    hasChanges = true
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp)
                                    .verticalScroll(rememberScrollState())
                                    .horizontalScroll(rememberScrollState()),
                                textStyle = TextStyle(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.Normal
                                ),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (content.isEmpty()) {
                                            Text(
                                                text = stringResource(R.string.components_input_hint),
                                                style = TextStyle(
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        } else {
                            // 只读模式 - 显示带行号的文本
                            TextEditorContent(
                                content = content,
                                isCode = isCodeFile
                            )
                        }
                    }
                }

                // 文件信息栏
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = stringResource(R.string.components_file_label, fileName),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.components_size_label, content.length),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(R.string.components_lines_label, content.lines().count()),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
}

/**
 * 文本编辑器内容显示（带行号）
 */
@Composable
private fun TextEditorContent(
    content: String,
    isCode: Boolean = false
) {
    val lines = content.lines()
    val lineCount = lines.size
    val lineNumberWidth = lineCount.toString().length + 2

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 行号列
        Column(
            modifier = Modifier
                .width((lineNumberWidth * 10).dp)
                .verticalScroll(verticalScrollState)
        ) {
            lines.forEachIndexed { index, _ ->
                Text(
                    text = "${index + 1}",
                    style = TextStyle(
                        fontFamily = FontFamily.Monospace,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (index % 2 == 0)
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                            else
                                Color.Transparent
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }

        // 内容列
        Box(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(horizontalScrollState)
                .verticalScroll(verticalScrollState)
        ) {
            Text(
                text = content.ifEmpty { " " },
                style = TextStyle(
                    fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Normal
                ),
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}

/**
 * 简单的文本预览组件（不包含编辑功能）
 */
@Composable
fun TextPreviewScreen(
    content: String,
    fileName: String = "",
    onBack: () -> Unit
) {
    TextEditorScreen(
        filePath = fileName,
        initialContent = content,
        onBack = onBack,
        onSave = null
    )
}
