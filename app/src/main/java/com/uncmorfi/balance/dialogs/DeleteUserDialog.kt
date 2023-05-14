package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.ARG_USER
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
        fun newInstance(user: User): DeleteUserDialog {
            return DeleteUserDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                }
            }
        }
    }
}
