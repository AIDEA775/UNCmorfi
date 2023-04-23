package com.uncmorfi.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.BaseDialogHelper
import kotlinx.android.synthetic.main.dialog_set_name.view.*

class SetNameDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val v = View.inflate(context, R.layout.dialog_set_name, null)
        builder.setView(v)

        v.setNameInput.append(user.name)

        v.setNameSave.setOnClickListener {
            viewModel.updateUserName(user.copy(name = v.setNameInput.text.toString()))
            dismiss()
        }

        v.setNameCancel.setOnClickListener { dismiss() }

        return showKeyboard(builder.create())
    }

    private fun showKeyboard(dialog: AlertDialog): AlertDialog {
        val window = dialog.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    companion object {
        fun newInstance(fragment: Fragment, code: Int, user: User): SetNameDialog {
            return newInstance(::SetNameDialog, fragment, code, user)
        }
    }
}