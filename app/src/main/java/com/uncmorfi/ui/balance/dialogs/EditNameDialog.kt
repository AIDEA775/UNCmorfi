package com.uncmorfi.ui.balance.dialogs

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.databinding.DialogSetNameBinding
import com.uncmorfi.shared.ARG_USER
import com.uncmorfi.shared.BaseDialogHelper

class EditNameDialog : BaseDialogHelper() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        super.init()

        val binding = DialogSetNameBinding.inflate(LayoutInflater.from(context),null,false)
        binding.setNameInput.append(user.name)

        val builder = MaterialAlertDialogBuilder(requireContext(), R.style.ThemeOverlay_App_MaterialAlertDialog)
            .setView(binding.root)
            .setNegativeButton(android.R.string.cancel) { _, _ -> dismiss() }
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.updateUserName(user.copy(name = binding.setNameInput.text.toString()))
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