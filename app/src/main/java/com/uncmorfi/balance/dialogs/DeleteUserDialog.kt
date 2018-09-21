package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment

import com.uncmorfi.R
import com.uncmorfi.balance.backend.BalanceBackend
import com.uncmorfi.balance.model.User

class DeleteUserDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(context!!)

        val user = arguments?.getSerializable(UserOptionsDialog.ARG_USER) as User?
        val backend = BalanceBackend.getInstance(requireContext())

        if (user != null) {
            val positiveListener = DialogInterface.OnClickListener { _, _ ->
                backend.deleteUser(user)
                dismiss()
            }

            val negativeListener = DialogInterface.OnClickListener { _, _ -> dismiss() }

            builder.setMessage(String.format(getString(R.string.balance_delete_user_title), user.name))
                    .setPositiveButton(getString(R.string.balance_delete_user_positive), positiveListener)
                    .setNegativeButton(getString(android.R.string.cancel), negativeListener)
        }
        return builder.create()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {

        /**
         * @param user Puede no contener todos los datos del usuario, pero necesita:
         * [User.id]
         * [User.name]
         */
        fun newInstance(user: User): DeleteUserDialog {
            val args = Bundle()

            args.putSerializable(UserOptionsDialog.ARG_USER, user)

            val fragment = DeleteUserDialog()
            fragment.arguments = args
            return fragment
        }
    }
}
