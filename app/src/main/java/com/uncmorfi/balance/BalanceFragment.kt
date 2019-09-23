package com.uncmorfi.balance

import android.content.Intent
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.models.User
import com.uncmorfi.shared.*
import com.uncmorfi.shared.ReserveStatus.NOCACHED
import com.uncmorfi.shared.SnackType.*
import com.uncmorfi.shared.StatusCode.UPDATE_SUCCESS
import com.uncmorfi.shared.StatusCode.USER_INSERTED
import com.uncmorfi.viewmodel.MainViewModel
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

        newUser.done {
            activity?.hideKeyboard()
            if (it.isNotBlank()) {
                updateUser(*parseUsers(it))
            }
        }
        newUser.scanner {
            // Inicia el lector de barras.
            // Devuelve el resultado por [.onActivityResult].
            val integrator = IntentIntegrator.forSupportFragment(this)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
            integrator.setPrompt(getString(R.string.balance_align_barcode))
            integrator.setBeepEnabled(false)
            integrator.setBarcodeImageEnabled(true)
            integrator.initiateScan()
        }


        mViewModel.status.observe(this, Observer {
            if (it == UPDATE_SUCCESS || it == USER_INSERTED) {
                newUser.clearText()
            }
        })

        mViewModel.allUsers().observe(this, Observer {
            mUserAdapter.setUsers(it)
            userList = it
        })

        mViewModel.reserveStatus.observe(this, Observer {
            mRootView.snack(it)
        })

        mViewModel.reserveTry.observe(this, Observer {
            if (it > 0) mRootView.snack(getString(R.string.snack_loop).format(it), LOADING)
        })
    }

    private fun initRecyclerAndAdapter() {
        val layoutManager = object : LinearLayoutManager(context) {
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

    private fun showUserOptionsDialog(user: User) {
        UserOptionsDialog.newInstance(this, USER_OPTIONS_CODE, user)
                .show(fragmentManager!!, "UserOptionsDialog")
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
            mRootView.snack(R.string.snack_empty_update, ERROR)
        } else {
            updateUser(*userList.toTypedArray())
        }
    }

    private fun copyAllUsers() {
        if (userList.isNotEmpty()) {
            context?.copyToClipboard("cards",userList.joinToString("\n") { it.card })
            mRootView.snack(R.string.snack_copied, FINISH)
        }
        else {
            mRootView.snack(R.string.snack_empty_copy, ERROR)
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            updateUser(*parseUsers(result.contents))
        } else {
            when (requestCode) {
                USER_OPTIONS_CODE -> {
                    val user = data.getUser()
                    updateUser(user)
                }
                else -> {
                    super.onActivityResult(requestCode, resultCode, data)
                }
            }
        }
    }

    private fun parseUsers(cards: String): Array<User> {
        val users = mutableListOf<User>()
        for (c in cards.split("\\s+".toRegex())) {
            users.add(User(c))
        }
        return users.toTypedArray()
    }

    private fun updateUser(vararg users: User) {
        if(users.any { it.card.length < 15 }) {
            mRootView.snack(R.string.snack_new_user_dumb, FINISH)
        } else {
            userList.map { u -> if (u.card in users.map { it.card }) u.isLoading = true }
            mUserAdapter.setUsers(userList)

            when {
                userList.any { it.isLoading } ->
                    mRootView.snack(R.string.snack_updating, LOADING)
                users.size == 1 ->
                    mRootView.snack(getNewUserMsg(users.first()), LOADING)
                else ->
                    mRootView.snack(R.string.snack_new_user_several_adds, LOADING)
            }

            mViewModel.downloadUsers(*users)
        }
    }

    private fun getNewUserMsg(user: User): String {
        return getString(R.string.snack_new_user_adding).format(user.card)
    }

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_balance)
    }

    override fun onStop() {
        super.onStop()
        activity?.hideKeyboard()
        newUser.clearFocus()
        mViewModel.reserveStatus.value = NOCACHED
    }

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/autoconsulta.php"
        private const val USER_OPTIONS_CODE = 1
    }
}