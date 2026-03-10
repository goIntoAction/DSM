package wang.zengye.dsm.ui.login

import androidx.lifecycle.viewModelScope
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wang.zengye.dsm.data.model.ServerAccount
import wang.zengye.dsm.ui.base.BaseViewModel
import wang.zengye.dsm.util.SettingsManager
import javax.inject.Inject

data class AccountsUiState(
    override val isLoading: Boolean = true,
    val accounts: List<ServerAccount> = emptyList(),
    val currentAccountId: String = "",
    override val error: String? = null,
    val showAddDialog: Boolean = false,
    val showEditDialog: Boolean = false,
    val editingAccount: ServerAccount? = null,
    val showDeleteDialog: Boolean = false,
    val deletingAccount: ServerAccount? = null
) : wang.zengye.dsm.ui.base.BaseState

@HiltViewModel
class AccountsViewModel @Inject constructor() : BaseViewModel<AccountsUiState, AccountsIntent, AccountsEvent>() {

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val listType = Types.newParameterizedType(List::class.java, ServerAccount::class.java)
    private val adapter = moshi.adapter<List<ServerAccount>>(listType)

    private val _state = MutableStateFlow(AccountsUiState())
    override val state: StateFlow<AccountsUiState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<AccountsEvent>(extraBufferCapacity = 1)
    override val events = _events.asSharedFlow()

    init {
        sendIntent(AccountsIntent.LoadAccounts)
    }

    override suspend fun processIntent(intent: AccountsIntent) {
        when (intent) {
            is AccountsIntent.LoadAccounts -> loadAccounts()
            is AccountsIntent.ShowAddDialog -> _state.update { it.copy(showAddDialog = true) }
            is AccountsIntent.HideAddDialog -> _state.update { it.copy(showAddDialog = false) }
            is AccountsIntent.ShowEditDialog -> _state.update { it.copy(showEditDialog = true, editingAccount = intent.account) }
            is AccountsIntent.HideEditDialog -> _state.update { it.copy(showEditDialog = false, editingAccount = null) }
            is AccountsIntent.ShowDeleteDialog -> _state.update { it.copy(showDeleteDialog = true, deletingAccount = intent.account) }
            is AccountsIntent.HideDeleteDialog -> _state.update { it.copy(showDeleteDialog = false, deletingAccount = null) }
            is AccountsIntent.AddAccount -> addAccount(intent.account)
            is AccountsIntent.UpdateAccount -> updateAccount(intent.oldAccount, intent.newAccount)
            is AccountsIntent.DeleteAccount -> deleteAccount(intent.account)
            is AccountsIntent.SetDefaultAccount -> setDefaultAccount(intent.account)
        }
    }

    private suspend fun loadAccounts() {
        val serversJson = SettingsManager.servers.value
        val currentHost = SettingsManager.host.first()
        val currentAccount = SettingsManager.account.first()

        val accounts: List<ServerAccount> = if (serversJson.isNotEmpty()) {
            try {
                adapter.fromJson(serversJson) ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        } else {
            emptyList()
        }

        // 标记当前账户
        val currentId = "$currentHost|$currentAccount"
        _state.update {
            it.copy(
                isLoading = false,
                accounts = accounts,
                currentAccountId = currentId
            )
        }
    }

    private suspend fun addAccount(account: ServerAccount) {
        val currentAccounts = _state.value.accounts.toMutableList()
        currentAccounts.add(account)
        saveAccounts(currentAccounts)
        _state.update { it.copy(showAddDialog = false) }
        _events.emit(AccountsEvent.AccountAdded)
    }

    private suspend fun updateAccount(oldAccount: ServerAccount, newAccount: ServerAccount) {
        val currentAccounts = _state.value.accounts.toMutableList()
        val index = currentAccounts.indexOf(oldAccount)
        if (index >= 0) {
            currentAccounts[index] = newAccount
            saveAccounts(currentAccounts)
        }
        _state.update { it.copy(showEditDialog = false, editingAccount = null) }
        _events.emit(AccountsEvent.AccountUpdated)
    }

    private suspend fun deleteAccount(account: ServerAccount) {
        val currentAccounts = _state.value.accounts.toMutableList()
        currentAccounts.remove(account)
        saveAccounts(currentAccounts)
        _state.update { it.copy(showDeleteDialog = false, deletingAccount = null) }
        _events.emit(AccountsEvent.AccountDeleted)
    }

    private suspend fun setDefaultAccount(account: ServerAccount) {
        SettingsManager.setHost(account.host)
        SettingsManager.setAccount(account.account)
        SettingsManager.setPassword(account.password) // 加密存储密码

        val currentId = "${account.host}|${account.account}"
        _state.update { it.copy(currentAccountId = currentId) }

        // 更新列表中的默认标记
        val accounts = _state.value.accounts.map {
            it.copy(isDefault = it.host == account.host && it.account == account.account)
        }
        saveAccounts(accounts)
        _events.emit(AccountsEvent.DefaultAccountChanged)
    }

    private suspend fun saveAccounts(accounts: List<ServerAccount>) {
        val json = adapter.toJson(accounts)
        SettingsManager.setServers(json)
        _state.update { it.copy(accounts = accounts) }
    }
}