package com.uncmorfi.balance

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.MainViewModel
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.*
import com.uncmorfi.shared.ReserveStatus.NOCACHED
import com.uncmorfi.shared.SnackType.*
import com.uncmorfi.shared.StatusCode.UPDATE_SUCCESS
import com.uncmorfi.shared.StatusCode.USER_INSERTED
import kotlinx.android.synthetic.main.fragment_balance.*

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a [UserAdapter] para llenar el RecyclerView.
 * Usa a [UserOptionsDialog] para que el usuario pueda modificar alguna tarjeta.
 */
class BalanceFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mUserAdapter: UserAdapter
    private val viewModel: MainViewModel by viewModels()

    private var userList: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        initRecyclerAndAdapter()

        newUser.init(this) { code ->
            activity?.hideKeyboard()
            if (code.isNotBlank()) {
                updateCards(parseCards(code))
            }
        }

        observe(viewModel.status) {
            if (it == UPDATE_SUCCESS || it == USER_INSERTED) {
                newUser.clearText()
            }
            mRootView.snack(it)
        }

        observe(viewModel.allUsers()) {
            mUserAdapter.setUsers(it)
            userList = it
        }
    }

    private fun initRecyclerAndAdapter() {
        val layoutManager = object : LinearLayoutManager(context) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        balanceList.isNestedScrollingEnabled = false
        balanceList.layoutManager = layoutManager

        mUserAdapter = UserAdapter(::showUserOptionsDialog) {
            updateCards(listOf(it.card))
        }

        balanceList.adapter = mUserAdapter
    }

    private fun showUserOptionsDialog(user: User) {
        UserOptionsDialog.newInstance(this, USER_OPTIONS_CODE, user)
            .show(parentFragmentManager, "UserOptionsDialog")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.balance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.balance_update -> {
                updateAllUsers(); true
            }
            R.id.balance_copy -> {
                copyAllUsers(); true
            }
            R.id.balance_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateAllUsers() {
        if (userList.isEmpty()) {
            mRootView.snack(R.string.snack_empty_update, ERROR)
        } else {
            updateCards(userList.map { it.card })
        }
    }

    private fun copyAllUsers() {
        if (userList.isNotEmpty()) {
            context?.copyToClipboard("cards", userList.joinToString("\n") { it.card })
            mRootView.snack(R.string.snack_copied, FINISH)
        } else {
            mRootView.snack(R.string.snack_empty_copy, ERROR)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            updateCards(parseCards(result.contents))
        } else {
            when (requestCode) {
                USER_OPTIONS_CODE -> {
                    val user = data.getUser()
                    updateCards(listOf(user.card))
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun parseCards(cards: String): List<String> {
        return cards.split("\\s+".toRegex())
    }

    private fun updateCards(cards: List<String>) {

        if (cards.any { it.length < 15 }) {
            mRootView.snack(R.string.snack_new_user_dumb, FINISH)
            return
        }

        userList.map { u -> if (u.card in cards) u.isLoading = true }
        mUserAdapter.setUsers(userList)

        when {
            userList.any { it.isLoading } -> mRootView.snack(R.string.snack_updating, LOADING)
            cards.size == 1 -> mRootView
                .snack(
                    getString(R.string.snack_new_user_adding).format(cards.first()), LOADING
                )
            else -> mRootView.snack(R.string.snack_new_user_several_adds, LOADING)
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
        newUser.clearFocus()
        viewModel.reservation.value = NOCACHED
    }

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/autoconsulta.php"
        private const val USER_OPTIONS_CODE = 1
    }
}