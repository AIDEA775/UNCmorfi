package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.models.User

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()
        val items = arrayOfNulls<CharSequence>(5)
        items[0] = getString(R.string.balance_user_options_update)
        items[1] = getString(R.string.balance_user_options_delete)
        items[2] = getString(R.string.balance_user_options_copy)
        items[3] = getString(R.string.balance_user_options_barcode)
        items[4] = getString(R.string.balance_user_options_set_name)

        builder.setTitle(getString(R.string.balance_user_options_title))
                .setItems(items) { _, which -> sendResult(which, user) }
        return builder.create()
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): UserOptionsDialog {
            return newInstance(::UserOptionsDialog, fragment, code, user)
        }
    }

}
