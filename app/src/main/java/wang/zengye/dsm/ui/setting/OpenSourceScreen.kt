package wang.zengye.dsm.ui.setting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import wang.zengye.dsm.R
import wang.zengye.dsm.ui.theme.*

/**
 * 开源库信息
 */
data class OpenSourceLibrary(
    val name: String,
    val version: String,
    val license: String,
    val url: String,
    val licenseText: String? = null
)

/**
 * 开源软件声明页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OpenSourceScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedLibrary by remember { mutableStateOf<OpenSourceLibrary?>(null) }

    // 开源库列表
    val libraries = remember {
        listOf(
            // ========== AndroidX & Jetpack Compose ==========
            OpenSourceLibrary(
                name = "Jetpack Compose",
                version = "BOM 2026.02.01",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/compose"
            ),
            OpenSourceLibrary(
                name = "Kotlin",
                version = "2.3.10",
                license = "Apache 2.0",
                url = "https://kotlinlang.org/"
            ),
            OpenSourceLibrary(
                name = "AndroidX Core KTX",
                version = "1.17.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/core"
            ),
            OpenSourceLibrary(
                name = "AndroidX Lifecycle",
                version = "2.10.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/topic/libraries/architecture/lifecycle"
            ),
            OpenSourceLibrary(
                name = "AndroidX Navigation Compose",
                version = "2.9.7",
                license = "Apache 2.0",
                url = "https://developer.android.com/guide/navigation"
            ),
            OpenSourceLibrary(
                name = "AndroidX DataStore",
                version = "1.2.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/topic/libraries/architecture/datastore"
            ),
            OpenSourceLibrary(
                name = "AndroidX Biometric",
                version = "1.1.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/biometric"
            ),
            OpenSourceLibrary(
                name = "AndroidX Security Crypto",
                version = "1.1.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/security"
            ),
            OpenSourceLibrary(
                name = "AndroidX Room",
                version = "2.7.0-alpha12",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/room"
            ),
            OpenSourceLibrary(
                name = "AndroidX ConstraintLayout Compose",
                version = "1.1.1",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/constraintlayout"
            ),
            OpenSourceLibrary(
                name = "AndroidX DocumentFile",
                version = "1.0.1",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/documentfile"
            ),
            OpenSourceLibrary(
                name = "AndroidX Activity Compose",
                version = "1.12.4",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/activity"
            ),
            OpenSourceLibrary(
                name = "Hilt",
                version = "2.58",
                license = "Apache 2.0",
                url = "https://dagger.dev/hilt/"
            ),
            OpenSourceLibrary(
                name = "Hilt Navigation Compose",
                version = "1.3.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/hilt"
            ),
            // ========== 网络 & JSON ==========
            OpenSourceLibrary(
                name = "Retrofit",
                version = "3.0.0",
                license = "Apache 2.0",
                url = "https://square.github.io/retrofit/"
            ),
            OpenSourceLibrary(
                name = "OkHttp",
                version = "5.3.2",
                license = "Apache 2.0",
                url = "https://square.github.io/okhttp/"
            ),
            OpenSourceLibrary(
                name = "Moshi",
                version = "1.15.2",
                license = "Apache 2.0",
                url = "https://github.com/square/moshi"
            ),
            OpenSourceLibrary(
                name = "Kotlinx Serialization",
                version = "1.7.1",
                license = "Apache 2.0",
                url = "https://github.com/Kotlin/kotlinx.serialization"
            ),
            // ========== 图片加载 ==========
            OpenSourceLibrary(
                name = "Coil",
                version = "2.7.0",
                license = "Apache 2.0",
                url = "https://coil-kt.github.io/coil/"
            ),
            OpenSourceLibrary(
                name = "Telephoto (Zoomable Image)",
                version = "0.18.0",
                license = "Apache 2.0",
                url = "https://github.com/saket/telephoto"
            ),
            // ========== 图表 ==========
            OpenSourceLibrary(
                name = "Vico",
                version = "3.0.2",
                license = "Apache 2.0",
                url = "https://github.com/patrykandpatrick/vico"
            ),
            // ========== 视频播放 ==========
            OpenSourceLibrary(
                name = "MPV Android",
                version = "0.1.9",
                license = "LGPL 3.0",
                url = "https://github.com/mpv-android/mpv-android"
            ),
            OpenSourceLibrary(
                name = "Seeker",
                version = "2.0.1",
                license = "Apache 2.0",
                url = "https://github.com/abdallahmehiz/seeker"
            ),
            // ========== SSH 终端 ==========
            OpenSourceLibrary(
                name = "ConnectBot SSHLib",
                version = "2.2.43",
                license = "Apache 2.0",
                url = "https://github.com/connectbot/connectbot"
            ),
            OpenSourceLibrary(
                name = "ConnectBot TermLib",
                version = "0.0.18",
                license = "Apache 2.0",
                url = "https://github.com/connectbot/connectbot"
            ),
            // ========== 其他 ==========
            OpenSourceLibrary(
                name = "ZXing (QR Code)",
                version = "3.5.4",
                license = "Apache 2.0",
                url = "https://github.com/zxing/zxing"
            ),
            OpenSourceLibrary(
                name = "Kotlinx Coroutines",
                version = "1.8.0",
                license = "Apache 2.0",
                url = "https://github.com/Kotlin/kotlinx.coroutines"
            ),
            // ========== 测试库 ==========
            OpenSourceLibrary(
                name = "JUnit 4",
                version = "4.13.2",
                license = "EPL 1.0",
                url = "https://junit.org/junit4/"
            ),
            OpenSourceLibrary(
                name = "MockK",
                version = "1.13.10",
                license = "Apache 2.0",
                url = "https://mockk.io/"
            ),
            OpenSourceLibrary(
                name = "Turbine",
                version = "1.0.0",
                license = "Apache 2.0",
                url = "https://github.com/cashapp/turbine"
            ),
            OpenSourceLibrary(
                name = "Truth",
                version = "1.4.2",
                license = "Apache 2.0",
                url = "https://truth.dev/"
            ),
            OpenSourceLibrary(
                name = "AndroidX Test JUnit",
                version = "1.3.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/test"
            ),
            OpenSourceLibrary(
                name = "AndroidX Espresso",
                version = "3.7.0",
                license = "Apache 2.0",
                url = "https://developer.android.com/jetpack/androidx/releases/test"
            )
        )
    }

    // 许可证详情对话框
    selectedLibrary?.let { library ->
        LicenseDetailDialog(
            library = library,
            onDismiss = { selectedLibrary = null }
        )
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.open_source_title)) },
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
        ) {
            // 描述
            Text(
                text = stringResource(R.string.open_source_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Medium)
            )

            HorizontalDivider()

            // 库列表
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(libraries) { library ->
                    LibraryItem(
                        library = library,
                        onClick = { selectedLibrary = library }
                    )
                }
            }
        }
    }
}

@Composable
private fun LibraryItem(
    library: OpenSourceLibrary,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.PageHorizontal, vertical = Spacing.Small)
            .clickable(onClick = onClick),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.cardSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = library.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.Small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.open_source_version) + ": " + library.version,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = library.license,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Icon(
                imageVector = Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LicenseDetailDialog(
    library: OpenSourceLibrary,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.8f),
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
                        text = library.name,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.common_close))
                    }
                }

                HorizontalDivider()

                // 内容
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(Spacing.Medium)
                ) {
                    // 版本和许可证信息
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.Large)
                    ) {
                        Column {
                            Text(
                                text = stringResource(R.string.open_source_version),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = library.version,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Column {
                            Text(
                                text = stringResource(R.string.open_source_licenses),
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = library.license,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    if (library.url.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.Medium))
                        Text(
                            text = library.url,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // 许可证文本
                    if (!library.licenseText.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.height(Spacing.Medium))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(Spacing.Medium))

                        Text(
                            text = stringResource(R.string.open_source_licenses),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(Spacing.Small))

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = library.licenseText,
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            )
                        }
                    }
                }

                // 关闭按钮
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.Medium)
                ) {
                    Text(stringResource(R.string.common_close))
                }
            }
        }
    }
}
