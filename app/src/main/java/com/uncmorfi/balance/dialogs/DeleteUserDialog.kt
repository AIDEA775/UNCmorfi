package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.BaseDialogHelper

class DeleteUserDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()
        return builder.setMessage(getString(R.string.balance_delete_user_title, user.name))
            .setNegativeButton(getString(android.R.string.cancel)) { _, _ -> dismiss() }
            .setPositiveButton(getString(R.string.balance_delete_user_positive)) { _, _ ->
                viewModel.deleteUser(user)
                dismiss()
            }
            .create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): DeleteUserDialog {
            return newInstance(::DeleteUserDialog, fragment, code, user)
        }
    }
}
