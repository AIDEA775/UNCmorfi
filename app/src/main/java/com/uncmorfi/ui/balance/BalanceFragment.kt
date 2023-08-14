package com.uncmorfi.ui.balance

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.ui.balance.dialogs.UserOptionsDialog
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.databinding.FragmentBalanceBinding
import com.uncmorfi.shared.*
import com.uncmorfi.shared.SnackType.*
import com.uncmorfi.shared.StatusCode.UPDATE_SUCCESS
import com.uncmorfi.shared.StatusCode.USER_INSERTED

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a [UserAdapter] para llenar el RecyclerView.
 * Usa a [UserOptionsDialog] para que el usuario pueda modificar alguna tarjeta.
 */
class BalanceFragment : Fragment(R.layout.fragment_balance) {

    private lateinit var binding : FragmentBalanceBinding

    private lateinit var mUserAdapter: UserAdapter
    private val viewModel: MainViewModel by activityViewModels()

    private var userList: List<User> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding = FragmentBalanceBinding.bind(view)
        binding.setUi()

        observe(viewModel.status) {
            if (it == UPDATE_SUCCESS || it == USER_INSERTED) {
                binding.newUser.clearText()
            }
        }

        observe(viewModel.getAllUsers()) {
            mUserAdapter.setUsers(it)
            userList = it
        }
    }

    private fun FragmentBalanceBinding.setUi(){
        addMenu(R.menu.balance){ menuItemId ->
            when (menuItemId) {
                R.id.balance_update -> {
                    updateAllUsers(); true
                }
                R.id.balance_copy -> {
                    copyAllUsers(); true
                }
                R.id.balance_browser -> requireActivity().startBrowser(HUEMUL_AUTOCONSULTA_URL)
                else -> false
            }
        }

        newUser.onDone { code ->
            activity?.hideKeyboard()
            updateCards(parseCards(code))
        }

        initRecyclerAndAdapter()
    }
    private fun initRecyclerAndAdapter() {
        val layoutManager = object : LinearLayoutManager(context) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        binding.balanceList.isNestedScrollingEnabled = false
        binding.balanceList.layoutManager = layoutManager

        mUserAdapter = UserAdapter(::showUserOptionsDialog) {
            updateCards(listOf(it.card))
        }

        binding.balanceList.adapter = mUserAdapter
    }

    private fun showUserOptionsDialog(user: User) {
        UserOptionsDialog.newInstance(user)
            .show(parentFragmentManager, "UserOptionsDialog")
    }

    private fun updateAllUsers() {
        if (userList.isEmpty()) {
            binding.root.snack(R.string.snack_empty_update, ERROR)
        } else {
            updateCards(userList.map { it.card })
        }
    }

    private fun copyAllUsers() {
        if (userList.isNotEmpty()) {
            context?.copyToClipboard("cards", userList.joinToString("\n") { it.card })
            binding.root.snack(R.string.snack_copied, FINISH)
        } else {
            binding.root.snack(R.string.snack_empty_copy, ERROR)
        }
    }

    private fun parseCards(cards: String): List<String> {
        return cards.split("\\s+".toRegex())
    }

    private fun updateCards(cards: List<String>) {

        if (cards.any { it.length < 15 }) {
            binding.root.snack(R.string.snack_new_user_dumb, FINISH)
            return
        }

        userList.map { u -> if (u.card in cards) u.isLoading = true }
        mUserAdapter.setUsers(userList)

        when {
            userList.any { it.isLoading } -> binding.root.snack(R.string.snack_updating, LOADING)
            cards.size == 1 -> binding.root
                .snack(
                    getString(R.string.snack_new_user_adding).format(cards.first()), LOADING
                )
            else -> binding.root.snack(R.string.snack_new_user_several_adds, LOADING)
        }

        // TODO
        viewModel.updateCards(cards.first())
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_balance)
    }

    override fun onStop() {
        super.onStop()
        activity?.hideKeyboard()
        binding.newUser.clearFocus()
    }

}