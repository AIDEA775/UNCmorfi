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
import com.uncmorfi.databinding.DialogUserBinding
import com.uncmorfi.shared.*

/**
 * Muestra las opciones disponibles para efectuar sobre un usuario.
 */
class UserOptionsDialog : BottomSheetDialogFragment() {

    val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding : DialogUserBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogUserBinding.inflate(LayoutInflater.from(context),container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val card = arguments?.getSerializable(ARG_CARD) as String

        observe(viewModel.state) { state ->
            binding.userProgressBar.invisible(state != StatusCode.UPDATING)
            binding.userUpdate.invisible(state == StatusCode.UPDATING)
        }

        observe(viewModel.getUser(card)) { user ->
            if (user == null) {
                dismiss()
                return@observe
            }

            binding.userName.text = user.name
            binding.userEmail.text = user.email
            binding.userType.text = user.type

            binding.userBalance.text = user.balanceOrRations()
            binding.userPrice.text =
                if (user.rations != null) getString(R.string.balance_user_options_rations)
                else getString(R.string.balance_user_options_price, user.price?.toMoneyFormat())

            binding.userUpdate.setOnClickListener {
                viewModel.updateCards(user.card)
            }

            // https://github.com/material-components/material-components-android/issues/1952#issuecomment-1000997296
            binding.userCopyToClip.setOnClickListener {
                requireContext().copyToClipboard("card", user.card)
                val msg = getString(R.string.snack_copied_param, user.card)
                binding.userSnackbarHack.snack(msg, SnackType.FINISH)
            }

            binding.userEdit.setOnClickListener {
                EditNameDialog
                    .newInstance(user)
                    .show(parentFragmentManager, "EditNameDialog")
            }

            binding.userRecharge.setOnClickListener {
                requireActivity().startBrowser(SANAVIRON_URL)
            }

            binding.userReserve.setOnClickListener {
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