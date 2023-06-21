package com.uncmorfi.ui.balance.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.*
import kotlinx.android.synthetic.main.dialog_user.view.*

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog : BottomSheetDialogFragment() {

    val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_user, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val card = arguments?.getSerializable(ARG_CARD) as String

        observe(viewModel.status) {
            view.userProgressBar.invisible(it != StatusCode.UPDATING)
            view.userUpdate.invisible(it == StatusCode.UPDATING)
        }

        observe(viewModel.getUser(card)) { user ->
            if (user == null) {
                dismiss()
                return@observe
            }

            view.userName.text = user.name
            view.userEmail.text = user.email
            view.userType.text = user.type

            view.userBalance.text = user.balanceOrRations()
            view.userPrice.text =
                if (user.rations != null) getString(R.string.balance_user_options_rations)
                else getString(R.string.balance_user_options_price, user.price?.toMoneyFormat())

            view.userUpdate.setOnClickListener {
                viewModel.updateCards(user.card)
            }

            // https://github.com/material-components/material-components-android/issues/1952#issuecomment-1000997296
            view.userCopyToClip.setOnClickListener {
                requireContext().copyToClipboard("card", user.card)
                val msg = getString(R.string.snack_copied_param, user.card)
                view.userSnackbarHack.snack(msg, SnackType.FINISH)
            }

            view.userEdit.setOnClickListener {
                EditNameDialog
                    .newInstance(user)
                    .show(parentFragmentManager, "EditNameDialog")
            }

            view.userRecharge.setOnClickListener {
                requireActivity().startBrowser(SANAVIRON_URL)
            }

            view.userReserve.setOnClickListener {
                requireContext().copyToClipboard("card", user.card)
                Toast.makeText(
                    requireContext(),
                    R.string.balance_user_options_copy_toast,
                    Toast.LENGTH_SHORT
                ).show()
                requireActivity().startBrowser(HUEMUL_RESERVA_URL)
            }
        }
    }

    companion object {
        fun newInstance(user: User) = UserOptionsDialog().apply {
            arguments = Bundle().apply {
                putString(ARG_CARD, user.card)
            }
        }
    }
}
