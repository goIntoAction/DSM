package wang.zengye.dsm.ui.control_panel

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import wang.zengye.dsm.R
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import wang.zengye.dsm.util.formatSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageScreen(
    onNavigateBack: () -> Unit = {},
    onNavigateToSmartTest: (String) -> Unit = {},
    viewModel: StorageViewModel = hiltViewModel()
) {
    val uiState by viewModel.state.collectAsStateWithLifecycle()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    
    val tabs = listOf(
        stringResource(R.string.storage_tab_overview),
        stringResource(R.string.storage_tab_volumes),
        stringResource(R.string.storage_tab_pools),
        stringResource(R.string.storage_tab_disks),
        stringResource(R.string.storage_tab_hot_spare),
        stringResource(R.string.storage_tab_ssd_cache)
    )

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.storage_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    scrolledContainerColor = Color.Transparent
                ),
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(StorageIntent.LoadStorage) }) {
                        Icon(Icons.Default.Refresh, contentDescription = stringResource(R.string.dashboard_refresh))
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                edgePadding = 16.dp,
                containerColor = Color.Transparent
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                    )
                }
            }

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                uiState.error != null -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = uiState.error ?: stringResource(R.string.components_load_failed),
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.sendIntent(StorageIntent.LoadStorage) }) {
                            Text(stringResource(R.string.common_retry))
                        }
                    }
                }

                else -> {
                    when (selectedTabIndex) {
                        0 -> OverviewTab(uiState, onNavigateToSmartTest)
                        1 -> VolumesTab(uiState)
                        2 -> StoragePoolsTab(uiState, onNavigateToSmartTest)
                        3 -> DisksTab(uiState, onNavigateToSmartTest)
                        4 -> HotSpareTab(uiState)
                        5 -> SsdCacheTab(uiState)
                    }
                }
            }
        }
    }
}

@Composable
private fun OverviewTab(
    uiState: StorageUiState,
    onNavigateToSmartTest: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 存储空间状态
        if (uiState.volumes.isNotEmpty()) {
            item {
                SectionTitle(title = stringResource(R.string.storage_volumes_status))
            }
            items(uiState.volumes) { volume ->
                VolumeCard(volume = volume)
            }
        }

        // 存储池
        if (uiState.storagePools.isNotEmpty()) {
            item {
                SectionTitle(title = stringResource(R.string.storage_pools))
            }
            items(uiState.storagePools) { pool ->
                StoragePoolCard(pool = pool, showDetail = false)
            }
        }

        // 硬盘槽位
        uiState.env?.let { env ->
            item {
                SectionTitle(title = stringResource(R.string.storage_disk_bays))
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = stringResource(R.string.storage_bay_count, env.bayNumber),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(env.bayNumber) { index ->
                                val hasDisk = uiState.disks.any { it.numId == index + 1 }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp, 20.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(
                                            if (hasDisk) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun VolumesTab(uiState: StorageUiState) {
    if (uiState.volumes.isEmpty()) {
        EmptyState(message = stringResource(R.string.storage_no_volumes))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.volumes) { volume ->
                VolumeCard(volume = volume)
            }
        }
    }
}

@Composable
private fun StoragePoolsTab(
    uiState: StorageUiState,
    onNavigateToSmartTest: (String) -> Unit
) {
    if (uiState.storagePools.isEmpty()) {
        EmptyState(message = stringResource(R.string.storage_no_pools))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.storagePools) { pool ->
                StoragePoolDetailCard(
                    pool = pool,
                    disks = uiState.disks,
                    volumes = uiState.volumes,
                    onNavigateToSmartTest = onNavigateToSmartTest
                )
            }
        }
    }
}

@Composable
private fun DisksTab(
    uiState: StorageUiState,
    onNavigateToSmartTest: (String) -> Unit
) {
    if (uiState.disks.isEmpty()) {
        EmptyState(message = stringResource(R.string.storage_no_disks))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.disks) { disk ->
                DiskDetailCard(disk = disk, onClick = { onNavigateToSmartTest(disk.id) })
            }
        }
    }
}

@Composable
private fun HotSpareTab(uiState: StorageUiState) {
    if (uiState.hotSpares.isEmpty()) {
        EmptyState(message = stringResource(R.string.storage_no_hot_spare))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.hotSpares) { spare ->
                val relatedDisk = uiState.disks.find { it.id == spare.diskId }
                HotSpareCard(spare = spare, relatedDisk = relatedDisk)
            }
        }
    }
}

@Composable
private fun SsdCacheTab(uiState: StorageUiState) {
    if (uiState.ssdCaches.isEmpty()) {
        EmptyState(message = stringResource(R.string.storage_no_ssd_cache))
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.ssdCaches) { cache ->
                SsdCacheCard(cache = cache)
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun VolumeCard(volume: VolumeInfoUi) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = volume.name.ifEmpty { stringResource(R.string.storage_volume_id, volume.id) },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                StatusBadge(status = volume.status)
            }

            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (volume.fsType.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = volume.fsType,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            StorageProgressBar(
                used = volume.usedSize,
                total = volume.totalSize,
                usagePercent = volume.usagePercent
            )
        }
    }
}

@Composable
private fun StoragePoolCard(
    pool: StoragePoolUi,
    showDetail: Boolean = true
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.storage_pool_id, pool.numId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StatusBadge(status = pool.status)
                    Text(
                        text = formatSize(pool.totalSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = getDeviceTypeDisplay(pool.deviceType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pool.deviceType == "basic" || pool.deviceType == "shr_without_disk_protect") {
                    Text(
                        text = stringResource(R.string.storage_no_data_protection),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }

            if (showDetail) {
                Spacer(modifier = Modifier.height(8.dp))
                StorageProgressBar(
                    used = pool.usedSize,
                    total = pool.totalSize,
                    usagePercent = (pool.usage * 100).toInt()
                )
            }
        }
    }
}

@Composable
private fun StoragePoolDetailCard(
    pool: StoragePoolUi,
    disks: List<DiskInfoUi>,
    volumes: List<VolumeInfoUi>,
    onNavigateToSmartTest: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 标题行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.storage_pool_id, pool.numId),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = pool.status)
            }

            Text(
                text = "${getDeviceTypeDisplay(pool.deviceType)} - ${formatSize(pool.usedSize)} / ${formatSize(pool.totalSize)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 硬盘信息
            Text(
                text = stringResource(R.string.storage_disk_info),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            
            val poolDisks = disks.filter { pool.diskIds.contains(it.id) }
            poolDisks.forEach { disk ->
                DiskCompactCard(disk = disk, onClick = { onNavigateToSmartTest(disk.id) })
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Hot Spare 硬盘
            if (pool.spares.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.storage_hot_spare_disks),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.storage_hot_spare_count, pool.spares.size),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (pool.spares.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pool.spares.forEach { spareId ->
                                val spareDisk = disks.find { it.id == spareId }
                                if (spareDisk != null) {
                                    HotSpareDiskChip(disk = spareDisk)
                                }
                            }
                        }
                    }
                }
            }

            // 存储分配
            val poolVolumes = volumes.filter { it.poolPath == pool.id }
            if (poolVolumes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.storage_allocation),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                poolVolumes.forEach { volume ->
                    VolumeCard(volume = volume)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HotSpareDiskChip(disk: DiskInfoUi) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.tertiaryContainer,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Storage,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Column {
                Text(
                    text = disk.longName.ifEmpty { disk.name },
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = formatSize(disk.totalSize),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiskCompactCard(
    disk: DiskInfoUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (disk.isSsd) Icons.Default.Storage else Icons.Default.Storage,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = disk.longName.ifEmpty { disk.name },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${disk.model} - ${formatSize(disk.totalSize)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            StatusBadge(status = disk.overviewStatus.ifEmpty { disk.status })
        }
    }
}

@Composable
private fun DiskDetailCard(
    disk: DiskInfoUi,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (disk.isSsd) Icons.Default.Storage else Icons.Default.Storage,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = disk.longName.ifEmpty { disk.name },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = disk.model,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    if (disk.temperature >= 0) {
                        Text(
                            text = "${disk.temperature}°C",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (disk.temperature > 60) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Text(
                        text = formatSize(disk.totalSize),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 详细信息
            DiskInfoRow(label = stringResource(R.string.storage_disk_location), value = disk.container.ifEmpty { "-" })
            DiskInfoRow(label = stringResource(R.string.storage_disk_type), value = disk.diskType.ifEmpty { "-" })
            
            val poolName = disk.usedBy // 可以进一步映射到存储池名称
            DiskInfoRow(label = stringResource(R.string.storage_disk_pool), value = poolName.ifEmpty { "-" })
            
            DiskInfoRow(
                label = stringResource(R.string.storage_disk_status),
                value = getDiskStatusDisplay(disk.status),
                valueColor = if (disk.status == "normal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
            
            DiskInfoRow(
                label = stringResource(R.string.storage_disk_health),
                value = disk.smartStatus.ifEmpty { "-" },
                valueColor = if (disk.smartStatus == "normal") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )

            if (disk.remainLife >= 0) {
                DiskInfoRow(label = stringResource(R.string.storage_disk_remain_life), value = "${disk.remainLife}%")
            }

            if (disk.unc >= 0) {
                DiskInfoRow(label = stringResource(R.string.storage_disk_bad_sectors), value = disk.unc.toString())
            }

            if (disk.serial.isNotEmpty()) {
                DiskInfoRow(label = stringResource(R.string.storage_disk_serial), value = disk.serial)
            }

            if (disk.firm.isNotEmpty()) {
                DiskInfoRow(label = stringResource(R.string.storage_disk_firmware), value = disk.firm)
            }

            DiskInfoRow(label = stringResource(R.string.storage_disk_4kn), value = if (disk.is4Kn) stringResource(R.string.common_yes) else stringResource(R.string.common_no))
        }
    }
}

@Composable
private fun DiskInfoRow(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(120.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = valueColor
        )
    }
}

@Composable
private fun HotSpareCard(spare: HotSpareUi, relatedDisk: DiskInfoUi?) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = spare.name.ifEmpty { spare.id },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = spare.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 基本信息行
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.storage_hot_spare_size, formatSize(spare.size)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                relatedDisk?.let { disk ->
                    if (disk.temperature > 0) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = when {
                                    disk.temperature >= 60 -> Color(0xFFE53935)
                                    disk.temperature >= 50 -> Color(0xFFFF9800)
                                    else -> Color(0xFF4CAF50)
                                }
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.storage_disk_temp, disk.temperature),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // 关联硬盘详细信息
            relatedDisk?.let { disk ->
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // 硬盘型号
                if (disk.model.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = disk.model,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // 硬盘类型和健康状态
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // 硬盘类型标签
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = if (disk.isSsd) MaterialTheme.colorScheme.secondaryContainer
                        else MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Text(
                            text = if (disk.isSsd) "SSD" else "HDD",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            color = if (disk.isSsd) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }

                    // 健康状态
                    if (disk.health.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when (disk.health.lowercase()) {
                                "normal", "good", "healthy" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                "warning" -> Color(0xFFFF9800).copy(alpha = 0.15f)
                                else -> Color(0xFFE53935).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = disk.health,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = when (disk.health.lowercase()) {
                                    "normal", "good", "healthy" -> Color(0xFF4CAF50)
                                    "warning" -> Color(0xFFFF9800)
                                    else -> Color(0xFFE53935)
                                }
                            )
                        }
                    }

                    // SMART 状态
                    if (disk.smartStatus.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = when (disk.smartStatus.lowercase()) {
                                "normal", "good", "healthy" -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                                else -> Color(0xFFE53935).copy(alpha = 0.15f)
                            }
                        ) {
                            Text(
                                text = "SMART: ${disk.smartStatus}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = when (disk.smartStatus.lowercase()) {
                                    "normal", "good", "healthy" -> Color(0xFF4CAF50)
                                    else -> Color(0xFFE53935)
                                }
                            )
                        }
                    }
                }

                // SSD 预计寿命
                if (disk.isSsd && disk.remainLife >= 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.storage_disk_remain_life),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        LinearProgressIndicator(
                            progress = { disk.remainLife / 100f },
                            modifier = Modifier
                                .weight(1f)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = when {
                                disk.remainLife >= 80 -> Color(0xFF4CAF50)
                                disk.remainLife >= 50 -> Color(0xFFFF9800)
                                else -> Color(0xFFE53935)
                            },
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${disk.remainLife}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                disk.remainLife >= 80 -> Color(0xFF4CAF50)
                                disk.remainLife >= 50 -> Color(0xFFFF9800)
                                else -> Color(0xFFE53935)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SsdCacheCard(cache: SsdCacheUi) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = cache.id.replace("ssd_", stringResource(R.string.storage_ssd_cache_prefix)),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                StatusBadge(status = cache.status)
            }

            Spacer(modifier = Modifier.height(12.dp))

            StorageProgressBar(
                used = cache.usedSize,
                total = cache.totalSize,
                usagePercent = (cache.usage * 100).toInt()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (cache.type.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = cache.type,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Surface(
                    color = if (cache.readOnly) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.primaryContainer,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(
                        text = if (cache.readOnly) stringResource(R.string.storage_read_only) else stringResource(R.string.storage_read_write),
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun StorageProgressBar(
    used: Long,
    total: Long,
    usagePercent: Int
) {
    val progress = if (total > 0) used.toFloat() / total else 0f
    val color = when {
        progress > 0.9f -> MaterialTheme.colorScheme.error
        progress > 0.7f -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }

    LinearProgressIndicator(
        progress = { progress },
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = color,
        trackColor = MaterialTheme.colorScheme.surfaceVariant,
        strokeCap = StrokeCap.Round,
        gapSize = 0.dp,
        drawStopIndicator = {}
    )

    Spacer(modifier = Modifier.height(4.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "${formatSize(used)} / ${formatSize(total)}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "$usagePercent%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatusBadge(status: String) {
    val (text, containerColor, contentColor) = when (status) {
        "normal" -> Triple(
            stringResource(R.string.storage_status_normal),
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        "background" -> Triple(
            stringResource(R.string.storage_status_checking),
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer
        )
        "attention" -> Triple(
            stringResource(R.string.storage_status_attention),
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        "degrade", "degraded" -> Triple(
            stringResource(R.string.storage_status_degraded),
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        else -> Triple(
            status,
            MaterialTheme.colorScheme.surfaceVariant,
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getDeviceTypeDisplay(deviceType: String): String {
    return when (deviceType) {
        "basic" -> "Basic"
        "shr_without_disk_protect", "shr" -> "Synology Hybrid RAID (SHR)"
        "raid1" -> "RAID 1"
        "raid5" -> "RAID 5"
        "raid6" -> "RAID 6"
        "raid10" -> "RAID 10"
        else -> deviceType.uppercase()
    }
}

private fun getDiskStatusDisplay(status: String): String {
    return when (status) {
        "normal" -> "正常"
        "not_use" -> "未初始化"
        "sys_partition_normal" -> "已初始化"
        else -> status
    }
}