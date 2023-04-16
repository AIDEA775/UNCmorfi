package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.BaseDialogHelper

class DeleteUserDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            viewModel.deleteUser(user)
            dismiss()
        }
        val negativeListener = DialogInterface.OnClickListener { _, _ -> dismiss() }

        builder.setMessage(getString(R.string.balance_delete_user_title).format(user.name))
                .setPositiveButton(getString(R.string.balance_delete_user_positive), positiveListener)
                .setNegativeButton(getString(android.R.string.cancel), negativeListener)

        return builder.create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): DeleteUserDialog {
            return newInstance(::DeleteUserDialog, fragment, code, user)
        }
    }
}
