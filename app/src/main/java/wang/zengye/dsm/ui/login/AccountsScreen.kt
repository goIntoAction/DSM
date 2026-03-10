package wang.zengye.dsm.ui.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import wang.zengye.dsm.R
import wang.zengye.dsm.data.model.ServerAccount
import wang.zengye.dsm.ui.components.EmptyState
import wang.zengye.dsm.ui.components.LoadingState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    onNavigateBack: () -> Unit,
    onAccountSelected: (ServerAccount) -> Unit = {},
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // 处理事件
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AccountsEvent.AccountAdded -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
                is AccountsEvent.AccountUpdated -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
                is AccountsEvent.AccountDeleted -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
                is AccountsEvent.DefaultAccountChanged -> {
                    Toast.makeText(context, context.getString(R.string.common_save_success), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.accounts_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.sendIntent(AccountsIntent.ShowAddDialog) }) {
                        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.common_add))
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> {
                LoadingState(
                    message = stringResource(R.string.accounts_loading),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            state.accounts.isEmpty() -> {
                EmptyState(
                    message = stringResource(R.string.accounts_empty),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(state.accounts, key = { "${it.host}|${it.account}" }) { account ->
                        AccountItem(
                            account = account,
                            isCurrent = "${account.host}|${account.account}" == state.currentAccountId,
                            onSwitchClick = { viewModel.sendIntent(AccountsIntent.SetDefaultAccount(account)) },
                            onEditClick = { viewModel.sendIntent(AccountsIntent.ShowEditDialog(account)) },
                            onDeleteClick = { viewModel.sendIntent(AccountsIntent.ShowDeleteDialog(account)) },
                            onClick = { onAccountSelected(account) }
                        )
                    }
                }
            }
        }

        // 添加账户对话框
        if (state.showAddDialog) {
            AddEditAccountDialog(
                onDismiss = { viewModel.sendIntent(AccountsIntent.HideAddDialog) },
                onConfirm = { account ->
                    viewModel.sendIntent(AccountsIntent.AddAccount(account))
                }
            )
        }

        // 编辑账户对话框
        if (state.showEditDialog && state.editingAccount != null) {
            AddEditAccountDialog(
                account = state.editingAccount,
                onDismiss = { viewModel.sendIntent(AccountsIntent.HideEditDialog) },
                onConfirm = { newAccount ->
                    viewModel.sendIntent(AccountsIntent.UpdateAccount(state.editingAccount!!, newAccount))
                }
            )
        }

        // 删除确认对话框
        if (state.showDeleteDialog && state.deletingAccount != null) {
            AlertDialog(
                onDismissRequest = { viewModel.sendIntent(AccountsIntent.HideDeleteDialog) },
                title = { Text(stringResource(R.string.accounts_delete_title)) },
                text = { Text(stringResource(R.string.accounts_delete_message, state.deletingAccount!!.name)) },
                confirmButton = {
                    TextButton(
                        onClick = { viewModel.sendIntent(AccountsIntent.DeleteAccount(state.deletingAccount!!)) }
                    ) {
                        Text(stringResource(R.string.common_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.sendIntent(AccountsIntent.HideDeleteDialog) }) {
                        Text(stringResource(R.string.common_cancel))
                    }
                }
            )
        }
    }
}

@Composable
private fun AccountItem(
    account: ServerAccount,
    isCurrent: Boolean,
    onSwitchClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = null,
                    tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = account.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (isCurrent) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = stringResource(R.string.accounts_current),
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Text(
                        text = account.account,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = account.host,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    if (!isCurrent) {
                        IconButton(onClick = onSwitchClick) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = stringResource(R.string.accounts_switch),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.accounts_edit))
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.common_delete),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditAccountDialog(
    account: ServerAccount? = null,
    onDismiss: () -> Unit,
    onConfirm: (ServerAccount) -> Unit
) {
    var name by remember { mutableStateOf(account?.name ?: "") }
    var host by remember { mutableStateOf(account?.host ?: "") }
    var accountName by remember { mutableStateOf(account?.account ?: "") }
    var password by remember { mutableStateOf(account?.password ?: "") }
    var passwordVisible by remember { mutableStateOf(false) }
    var ssl by remember { mutableStateOf(account?.ssl ?: true) }
    var port by remember { mutableStateOf(account?.port?.toString() ?: "5000") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (account == null) stringResource(R.string.accounts_add_title) else stringResource(R.string.accounts_edit_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.accounts_name_label)) },
                    placeholder = { Text(stringResource(R.string.accounts_name_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it },
                    label = { Text(stringResource(R.string.accounts_address_label)) },
                    placeholder = { Text(stringResource(R.string.accounts_address_hint)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { newValue -> port = newValue.filter { c -> c.isDigit() } },
                        label = { Text(stringResource(R.string.accounts_port_label)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                    
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("HTTPS")
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(
                            checked = ssl,
                            onCheckedChange = { newValue -> ssl = newValue }
                        )
                    }
                }
                
                OutlinedTextField(
                    value = accountName,
                    onValueChange = { accountName = it },
                    label = { Text(stringResource(R.string.accounts_username_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.accounts_password_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) stringResource(R.string.login_hide_password) else stringResource(R.string.login_show_password)
                            )
                        }
                    }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val newAccount = ServerAccount(
                        name = name.ifEmpty { host },
                        host = host,
                        account = accountName,
                        password = password,
                        ssl = ssl,
                        port = port.toIntOrNull() ?: 5000,
                        isDefault = account?.isDefault ?: false
                    )
                    onConfirm(newAccount)
                },
                enabled = host.isNotEmpty() && accountName.isNotEmpty()
            ) {
                Text(stringResource(R.string.accounts_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

