package com.uncmorfi.balance

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.BaseDialogHelper
import com.uncmorfi.balance.dialogs.DeleteUserDialog
import com.uncmorfi.balance.dialogs.SetNameDialog
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.helpers.*
import com.uncmorfi.helpers.SnackType.*
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.models.User
import com.uncmorfi.viewmodel.MainViewModel
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.user_new.*

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a [UserAdapter] para llenar el RecyclerView.
 * Usa a [UserOptionsDialog] para que el usuario pueda modificar alguna tarjeta.
 */
class BalanceFragment : Fragment() {
    private lateinit var mRootView: View
    private lateinit var mUserAdapter: UserAdapter
    private lateinit var mViewModel: MainViewModel

    private var userList: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view

        mViewModel = ViewModelProvider(requireActivity()).get(MainViewModel::class.java)

        initRecyclerAndAdapter()
        initNewUserView()

        mViewModel.userStatus.observe(this, Observer {
            when (it) {
                BUSY -> {}
                UPDATED -> {
                    mRootView.snack(context, R.string.update_success, FINISH)
                    newUserInput.text.clear()
                }
                INSERTED -> {
                    mRootView.snack(context, R.string.new_user_success, FINISH)
                    newUserInput.text.clear()
                }
                DELETED -> mRootView.snack(context, R.string.balance_delete_user_msg, FINISH)
                else -> mRootView.snack(context, it)
            }
        })

        mViewModel.allUsers().observe(this, Observer {
            mUserAdapter.setUsers(it)
            userList = it
        })
    }

    private fun initRecyclerAndAdapter() {
        val layoutManager = object : androidx.recyclerview.widget.LinearLayoutManager(context) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        balanceList.isNestedScrollingEnabled = false
        balanceList.layoutManager = layoutManager

        mUserAdapter = UserAdapter(requireContext(),
                { showUserOptionsDialog(it) },
                { updateUser(it) })

        balanceList.adapter = mUserAdapter
    }

    private fun initNewUserView() {
        newUserInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        newUserInput.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                activity?.hideKeyboard()
                onNewUserClicked()
            }
            false
        }

        newUserInput.onTextChanged { onNewUserTextChanged(it) }

        // Por defecto llama al lector
        newUserScanner.setOnClickListener { callScanner() }
    }

    private fun onNewUserTextChanged(s: CharSequence) {
        newUserScanner.apply {
            if (s.isNotEmpty()) {
                // Cuando hay texto, cambia la función del botón por newUser.
                setImageResource(R.drawable.ic_done)
                contentDescription = getString(R.string.balance_new_user_button_enter)
                setOnClickListener {
                    activity?.hideKeyboard()
                    onNewUserClicked()
                }
            } else {
                // Si es está vacio, llama al lector.
                setImageResource(R.drawable.ic_barcode)
                contentDescription = getString(R.string.balance_new_user_button_code)
                setOnClickListener { callScanner() }
            }
        }
    }

    /**
     * Inicia el lector de barras.
     * Devuelve el resultado por [.onActivityResult].
     */
    private fun callScanner() {
        val integrator = IntentIntegrator.forSupportFragment(this)
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
        integrator.setPrompt(getString(R.string.balance_align_barcode))
        integrator.setBeepEnabled(false)
        integrator.setBarcodeImageEnabled(true)
        integrator.initiateScan()
    }

    private fun showUserOptionsDialog(user: User) {
        UserOptionsDialog.newInstance(this, USER_OPTIONS_CODE, user)
                .show(fragmentManager!!, "UserOptionsDialog")
    }

    private fun onNewUserClicked() {
        val input = newUserInput.text.toString()
        if (input.isNotEmpty())
            updateUser(*parseUsers(input))
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.balance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.balance_update -> { updateAllUsers(); true }
            R.id.balance_copy -> { copyAllUsers(); true}
            R.id.balance_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateAllUsers() {
        if (userList.isEmpty()) {
            mRootView.snack(context, R.string.balance_no_cards, ERROR)
        } else {
            updateUser(*userList.toTypedArray())
        }
    }

    private fun copyAllUsers() {
        if (userList.isNotEmpty()) {
            context?.copyToClipboard("cards", userList.joinToString(separator = "\n") { it.card })
            mRootView.snack(context, R.string.balance_copy_msg, SnackType.FINISH)
        }
        else {
            mRootView.snack(context, R.string.balance_no_cards, ERROR)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            updateUser(*parseUsers(result.contents))
        } else {
            when (requestCode) {
                USER_OPTIONS_CODE -> {
                    val user : User = getUserFromIntent(data)
                    when (resultCode) {
                        0 -> updateUser(user)
                        1 -> DeleteUserDialog.newInstance(this, DELETE_USER_CODE, user)
                                .show(fragmentManager!!, "DeleteUserDialog")
                        2 -> {
                            context?.copyToClipboard("card", user.card)
                            mRootView.snack(context, R.string.balance_copy_msg, SnackType.FINISH)
                        }
                        3 -> startActivity(BarcodeActivity.intent(context!!, user))
                        4 -> SetNameDialog.newInstance(this, SET_NAME_CODE, user)
                                .show(fragmentManager!!, "SetNameDialog")
                        else -> {
                        }
                    }
                }
                DELETE_USER_CODE -> {
                    mViewModel.deleteUser(getUserFromIntent(data))
                }
                SET_NAME_CODE -> {
                    mViewModel.updateUserName(getUserFromIntent(data))
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun getUserFromIntent(data: Intent?) : User {
        return data?.getSerializableExtra(BaseDialogHelper.ARG_USER) as User
    }

    private fun parseUsers(cards: String): Array<User> {
        val users = mutableListOf<User>()
        for (c in cards.split("\\s+".toRegex())) {
            users.add(User(c))
        }
        return users.toTypedArray()
    }

    private fun updateUser(vararg users: User) {
        when {
            users.any { it.card.length < 15 } -> {
                mRootView.snack(context, R.string.balance_new_user_dumb, FINISH)
            }
            context.isOnline() -> {

                userList.map { u -> if (u.card in users.map { it.card }) u.isLoading = true }
                mUserAdapter.setUsers(userList)

                when {
                    userList.any { it.isLoading } ->
                        mRootView.snack(context, R.string.updating, LOADING)
                    users.size == 1 ->
                        mRootView.snack(context, getNewUserMsg(users.first().card), LOADING)
                    else ->
                        mRootView.snack(context, R.string.balance_new_user_several_adds, LOADING)
                }

                mViewModel.downloadUsers(*users)
            }
            else -> {
                mRootView.snack(context, R.string.no_connection, ERROR)
            }
        }
    }

    private fun getNewUserMsg(card: String): String {
        return getString(R.string.balance_new_user_adding).format(card)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_balance)
    }

    override fun onStop() {
        super.onStop()
        activity?.hideKeyboard()
        newUserInput.clearFocus()
        mViewModel.userStatus.value = BUSY
    }

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/autoconsulta.php"
        private const val USER_OPTIONS_CODE = 1
        private const val SET_NAME_CODE = 2
        private const val DELETE_USER_CODE = 3
        const val BARCODE_PATH = "barcode-land-"
    }
}