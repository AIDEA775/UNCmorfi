package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.reservations.CaptchaDialog
import com.uncmorfi.reservations.ReserveOptionsDialog
import com.uncmorfi.shared.*
import kotlinx.android.synthetic.main.dialog_user.view.*
import kotlinx.android.synthetic.main.dialog_user.view.userBalance
import kotlinx.android.synthetic.main.dialog_user.view.userName

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog : BaseDialogHelper() {


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val v = View.inflate(context, R.layout.dialog_user, null)

        v.userName.text = user.name
        v.userEmail.text = user.email
        v.userType.text = user.type
        v.userBalance.text = user.balance.toMoneyFormat()
        v.userPrice.text =
            getString(R.string.balance_user_options_price, user.price.toMoneyFormat())

        Glide.with(requireContext())
            .load(user.image)
            .placeholder(R.drawable.ic_account)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .apply(RequestOptions.circleCropTransform())
            .into(v.userPhoto)

        builder.setView(v)

        return builder.create()
    }

    private var reserveCached = false

    fun onCreateDialog2(savedInstanceState: Bundle?): Dialog {
        reservationInit()
        val items = arrayOf(
            getString(R.string.balance_user_options_update),
            getString(R.string.balance_user_options_reserve),
            getString(R.string.balance_user_options_delete),
            getString(R.string.balance_user_options_copy),
//                getString(R.string.balance_user_options_barcode),
            getString(R.string.balance_user_options_set_name)
        )

        builder.setTitle(getString(R.string.balance_user_options_title))
            .setItems(items) { _, which ->
                when (which) {
                    0 -> sendResult(which, user)
                    1 -> reservation()
                    2 -> DeleteUserDialog
                        .newInstance(this, 0, user)
                        .show(parentFragmentManager, "DeleteUserDialog")
                    3 -> {
                        context?.copyToClipboard("card", user.card)
                        viewModel.status.value = StatusCode.COPIED
                    }
//                        4 -> startActivity(BarcodeActivity.intent(context!!, user))
                    4 -> SetNameDialog
                        .newInstance(this, 0, user)
                        .show(parentFragmentManager, "SetNameDialog")
                }
            }
        return builder.create()
    }

    private fun reservationInit() {
        viewModel.reservation.observe(this, Observer {
            reserveCached = reserveCached || (it == ReserveStatus.CACHED)
        })
        viewModel.reserveIsCached(user)
    }

    private fun reservation() {
        if (reserveCached) {
            ReserveOptionsDialog
                .newInstance(this, 0, user)
                .show(parentFragmentManager, "ReserveOptionsDialog")
        } else {
            CaptchaDialog
                .newInstance(this, 0, user)
                .show(parentFragmentManager, "CaptchaDialog")
        }
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): UserOptionsDialog {
            return newInstance(::UserOptionsDialog, fragment, code, user)
        }
    }

}
