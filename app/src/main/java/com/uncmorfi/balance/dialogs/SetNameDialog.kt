package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.app.AlertDialog
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText

import com.uncmorfi.R
import com.uncmorfi.balance.backend.BalanceBackend
import com.uncmorfi.balance.model.User

class SetNameDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())

        val v = View.inflate(context, R.layout.dialog_set_name, null)
        builder.setView(v)

        val user = arguments?.getSerializable(UserOptionsDialog.ARG_USER) as User?
        val backend = BalanceBackend.getInstance(requireContext())

        val input = v.findViewById<EditText>(R.id.set_name_input)
        val save = v.findViewById<Button>(R.id.set_name_save)
        val cancel = v.findViewById<Button>(R.id.set_name_cancel)

        if (user != null) {
            input.append(user.name)

            save.setOnClickListener {
                backend.updateNameOfUser(user, input.text.toString())
                dismiss()
            }

            cancel.setOnClickListener { dismiss() }
        }

        return showKeyboard(builder.create())
    }

    private fun showKeyboard(dialog: AlertDialog): AlertDialog {
        val window = dialog.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    override fun onPause() {
        super.onPause()
        dismiss()
    }

    companion object {

        /**
         * @param user Puede no contener todos los datos del usuario, pero necesita:
         * [User.name]
         */
        fun newInstance(user: User): SetNameDialog {
            val args = Bundle()

            args.putSerializable(UserOptionsDialog.ARG_USER, user)

            val fragment = SetNameDialog()
            fragment.arguments = args
            return fragment
        }
    }
}