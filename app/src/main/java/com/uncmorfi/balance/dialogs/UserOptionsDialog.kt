package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.balance.BarcodeActivity
import com.uncmorfi.helpers.StatusCode
import com.uncmorfi.helpers.copyToClipboard
import com.uncmorfi.models.User
import com.uncmorfi.reservations.CaptchaDialog

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog: BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()
        val items = arrayOf(
                getString(R.string.balance_user_options_update),
                "Reservar",
                getString(R.string.balance_user_options_delete),
                getString(R.string.balance_user_options_copy),
                getString(R.string.balance_user_options_barcode),
                getString(R.string.balance_user_options_set_name)
        )

        builder.setTitle(getString(R.string.balance_user_options_title))
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> sendResult(which, user)
                        1 -> CaptchaDialog.newInstance(this, 0, user)
                                .show(fragmentManager!!, "CaptchaDialog")
                        2 -> DeleteUserDialog.newInstance(this, 0, user)
                                .show(fragmentManager!!, "DeleteUserDialog")
                        3 -> {
                            context?.copyToClipboard("card", user.card)
                            viewModel.userStatus.value = StatusCode.COPIED
                        }
                        4 -> startActivity(BarcodeActivity.intent(context!!, user))
                        5 -> SetNameDialog.newInstance(this, 0, user)
                                .show(fragmentManager!!, "SetNameDialog")
                    }
                }
        return builder.create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): UserOptionsDialog {
            return newInstance(::UserOptionsDialog, fragment, code, user)
        }
    }

}
