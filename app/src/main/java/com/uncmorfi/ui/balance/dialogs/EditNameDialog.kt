package com.uncmorfi.ui.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.ARG_USER
import com.uncmorfi.shared.BaseDialogHelper
import kotlinx.android.synthetic.main.dialog_set_name.view.*

class EditNameDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val v = View.inflate(context, R.layout.dialog_set_name, null)
        v.setNameInput.append(user.name)

        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(v)
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.updateUserName(user.copy(name = v.setNameInput.text.toString()))
                dismiss()
            }
            .setNeutralButton(R.string.balance_user_options_delete) {_, _ ->
                DeleteUserDialog
                    .newInstance(user)
                    .show(parentFragmentManager, "DeleteUserDialog")
            }

        return showKeyboard(builder.create())
    }

    private fun showKeyboard(dialog: AlertDialog): AlertDialog {
        val window = dialog.window
        window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
        return dialog
    }

    companion object {
        fun newInstance(user: User): EditNameDialog {
            return EditNameDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_USER, user)
                }
            }
        }
    }
}