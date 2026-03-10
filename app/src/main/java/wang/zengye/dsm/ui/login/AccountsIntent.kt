package wang.zengye.dsm.ui.login

import wang.zengye.dsm.data.model.ServerAccount
import wang.zengye.dsm.ui.base.BaseIntent

/**
 * 账户管理 Intent
 */
sealed class AccountsIntent : BaseIntent {
    data object LoadAccounts : AccountsIntent()
    data object ShowAddDialog : AccountsIntent()
    data object HideAddDialog : AccountsIntent()
    data class ShowEditDialog(val account: ServerAccount) : AccountsIntent()
    data object HideEditDialog : AccountsIntent()
    data class ShowDeleteDialog(val account: ServerAccount) : AccountsIntent()
    data object HideDeleteDialog : AccountsIntent()
    data class AddAccount(val account: ServerAccount) : AccountsIntent()
    data class UpdateAccount(val oldAccount: ServerAccount, val newAccount: ServerAccount) : AccountsIntent()
    data class DeleteAccount(val account: ServerAccount) : AccountsIntent()
    data class SetDefaultAccount(val account: ServerAccount) : AccountsIntent()
}
