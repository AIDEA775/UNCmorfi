package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatDialogFragment
import com.uncmorfi.R
import com.uncmorfi.balance.BarcodeActivity
import com.uncmorfi.balance.BarcodeActivity.Companion.USER_ARG
import com.uncmorfi.balance.backend.BalanceBackend
import com.uncmorfi.balance.model.User

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog : AppCompatDialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val items = arrayOfNulls<CharSequence>(5)
        items[0] = getString(R.string.balance_user_options_update)
        items[1] = getString(R.string.balance_user_options_delete)
        items[2] = getString(R.string.balance_user_options_copy)
        items[3] = getString(R.string.balance_user_options_barcode)
        items[4] = getString(R.string.balance_user_options_set_name)

        val user = arguments?.getSerializable(ARG_USER) as User?
        val backend = BalanceBackend.getInstance(requireContext())

        if (user != null) {
            builder.setTitle(getString(R.string.balance_user_options_title))
                    .setItems(items) { _, which ->
                        when (which) {
                            0 -> backend.updateBalanceOfUser(
                                    user.card, intArrayOf(user.position))
                            1 -> DeleteUserDialog.newInstance(user)
                                    .show(fragmentManager!!, "DeleteUserDialog")
                            2 -> backend.copyCardToClipboard(user.card)
                            3 -> {
                                val intent = Intent(activity, BarcodeActivity::class.java)
                                intent.putExtra(USER_ARG, user)
                                startActivity(intent)
                            }
                            4 -> SetNameDialog.newInstance(user)
                                    .show(fragmentManager!!, "SetNameDialog")
                            else -> {
                            }
                        }
                    }
        }
        return builder.create()
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {
        const val ARG_USER = "user"

        /**
         * @param user Puede no contener todos los datos del usuario,
         * pero necesita además de los datos que necesitan las demás opciones:
         * [User.getCard]
         * [User.getPosition]
         * [User.getName]
         */
        fun newInstance(user: User): UserOptionsDialog {
            val args = Bundle()

            args.putSerializable(ARG_USER, user)

            val fragment = UserOptionsDialog()
            fragment.arguments = args

            return fragment
        }
    }
}
