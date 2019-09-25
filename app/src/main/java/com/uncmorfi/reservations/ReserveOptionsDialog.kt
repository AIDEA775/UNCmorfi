package com.uncmorfi.reservations

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.models.User
import com.uncmorfi.shared.BaseDialogHelper

class ReserveOptionsDialog: BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val items = mutableListOf(
                getString(R.string.reservations_consult),
                getString(R.string.reservations_reserve),
                getString(R.string.reservations_loop),
                getString(R.string.reservations_logout)
        )

        if (viewModel.reserveJob != null) {
            items.add(getString(R.string.reservations_stop))
        }

        builder.setTitle(user.card)
                .setItems(items.toTypedArray()) { _, which ->
                    when (which) {
                        0 -> viewModel.reserveConsult(user)
                        1 -> viewModel.reserve(user)
                        2 -> viewModel.reserveLoop(user)
                        3 -> viewModel.reserveLogout(user)
                        4 -> viewModel.reserveStop()
                    }
                }
        return builder.create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): ReserveOptionsDialog {
            return newInstance(::ReserveOptionsDialog, fragment, code, user)
        }
    }

}
