package com.uncmorfi.reservations

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.BaseDialogHelper
import com.uncmorfi.models.User

class ReserveOptionsDialog: BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        isCached()

        val items = mutableListOf(
                getString(R.string.reservations_consult),
                getString(R.string.reservations_reserve),
                getString(R.string.reservations_loop),
                getString(R.string.reservations_cancel),
                getString(R.string.reservations_loguot)
        )

        if (viewModel.reserveJob != null) {
            items.add(getString(R.string.reservations_stop))
        }

        builder.setTitle(user.card)
                .setItems(items.toTypedArray()) { _, which ->
                    when (which) {
                        0 -> if (isCached()) viewModel.reserve()
                        1 -> if (isCached()) viewModel.reserve()
                        2 -> if (isCached()) viewModel.reserveLoop()
                        3 -> {} // cancelar
                        4 -> {} // cerrar sesion
                        5 -> viewModel.reserveStop()
                    }
                }
        return builder.create()
    }

    private fun isCached(): Boolean {
        if (!viewModel.reservationIsCached(user)) {
            CaptchaDialog
                    .newInstance(this, 0, user)
                    .show(fragmentManager!!, "CaptchaDialog")
            return false
        }
        return true
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): ReserveOptionsDialog {
            return newInstance(::ReserveOptionsDialog, fragment, code, user)
        }
    }

}
