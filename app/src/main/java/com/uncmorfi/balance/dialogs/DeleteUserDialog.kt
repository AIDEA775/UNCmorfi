package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v4.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.balance.model.User

class DeleteUserDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val positiveListener = DialogInterface.OnClickListener { _, _ ->
            sendResult(0, user)
            dismiss()
        }
        val negativeListener = DialogInterface.OnClickListener { _, _ -> dismiss() }

        builder.setMessage(String.format(getString(R.string.balance_delete_user_title), user.name))
                .setPositiveButton(getString(R.string.balance_delete_user_positive), positiveListener)
                .setNegativeButton(getString(android.R.string.cancel), negativeListener)

        return builder.create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): DeleteUserDialog {
            return BaseDialogHelper.newInstance(::DeleteUserDialog, fragment, code, user)
        }
    }
}
