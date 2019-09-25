package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.uncmorfi.R
import com.uncmorfi.balance.BarcodeActivity
import com.uncmorfi.models.User
import com.uncmorfi.reservations.CaptchaDialog
import com.uncmorfi.reservations.ReserveOptionsDialog
import com.uncmorfi.shared.BaseDialogHelper
import com.uncmorfi.shared.ReserveStatus
import com.uncmorfi.shared.StatusCode
import com.uncmorfi.shared.copyToClipboard

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog: BaseDialogHelper() {
    private var reserveCached = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()
        reservationInit()
        val items = arrayOf(
                getString(R.string.balance_user_options_update),
                getString(R.string.balance_user_options_reserve),
                getString(R.string.balance_user_options_delete),
                getString(R.string.balance_user_options_copy),
                getString(R.string.balance_user_options_barcode),
                getString(R.string.balance_user_options_set_name)
        )

        builder.setTitle(getString(R.string.balance_user_options_title))
                .setItems(items) { _, which ->
                    when (which) {
                        0 -> sendResult(which, user)
                        1 -> reservation()
                        2 -> DeleteUserDialog
                                .newInstance(this, 0, user)
                                .show(fragmentManager!!, "DeleteUserDialog")
                        3 -> {
                            context?.copyToClipboard("card", user.card)
                            viewModel.status.value = StatusCode.COPIED
                        }
                        4 -> startActivity(BarcodeActivity.intent(context!!, user))
                        5 -> SetNameDialog
                                .newInstance(this, 0, user)
                                .show(fragmentManager!!, "SetNameDialog")
                    }
                }
        return builder.create()
    }

    private fun reservationInit() {
        viewModel.reserveStatus.observe(this, Observer {
            reserveCached = reserveCached || (it == ReserveStatus.CACHED)
        })
        viewModel.reserveIsCached(user)
    }

    private fun reservation() {
        if (reserveCached) {
            ReserveOptionsDialog
                    .newInstance(this, 0, user)
                    .show(fragmentManager!!, "ReserveOptionsDialog")
        } else {
            CaptchaDialog
                    .newInstance(this, 0, user)
                    .show(fragmentManager!!, "CaptchaDialog")
        }
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): UserOptionsDialog {
            return newInstance(::UserOptionsDialog, fragment, code, user)
        }
    }

}
