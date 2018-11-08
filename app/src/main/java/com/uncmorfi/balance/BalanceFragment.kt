package com.uncmorfi.balance

import android.content.*
import android.database.Cursor
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.InputFilter
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.R
import com.uncmorfi.balance.dialogs.BaseDialogHelper
import com.uncmorfi.balance.dialogs.DeleteUserDialog
import com.uncmorfi.balance.dialogs.SetNameDialog
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.balance.model.User
import com.uncmorfi.balance.model.UserProvider
import com.uncmorfi.balance.model.UsersContract
import com.uncmorfi.helpers.*
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.user_new.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a [UserCursorAdapter] para llenar el RecyclerView.
 * Usa a [UserOptionsDialog] para que el usuario pueda modificar alguna tarjeta.
 *
 * Se encarga del manejo de datos de los usuarios/tarjetas.
 * Se comunica con la base de datos a través de un [ContentResolver].
 * Usa a [DownloadUserAsyncTask] para descargar los datos de los usuarios.
 */
class BalanceFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor> {
    private lateinit var mRootView: View
    private lateinit var mUserCursorAdapter: UserCursorAdapter
    private val mContentResolver: ContentResolver by lazy { context!!.contentResolver }
    private val allUsers: Cursor
        get() = mContentResolver.query(
                UserProvider.CONTENT_URI,
                null, null, null, null, null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_balance, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loaderManager.initLoader(0, null, this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mRootView = view
        initRecyclerAndAdapter()
        initNewUserView()
    }

    private fun initRecyclerAndAdapter() {
        val layoutManager = object : LinearLayoutManager(context) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        balanceList.isNestedScrollingEnabled = false
        balanceList.layoutManager = layoutManager

        mUserCursorAdapter = UserCursorAdapter(requireContext(),
                { showUserOptionsDialog(it) },
                { updateBalance(it) })

        balanceList.adapter = mUserCursorAdapter
        balanceList.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun initNewUserView() {
        newUserInput.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        newUserInput.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                callNewUser()
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
                setImageResource(R.drawable.ic_check)
                contentDescription = getString(R.string.balance_new_user_button_enter)
                setOnClickListener {
                    hideKeyboard()
                    callNewUser()
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
                .show(fragmentManager, "UserOptionsDialog")
    }

    private fun callNewUser() {
        val input = newUserInput.text.toString().replace(" ", ",")
        if (input.isNotEmpty())
            newUser(input)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.balance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.balance_update -> { updateAllUsers(); true }
            R.id.balance_browser -> requireActivity().startBrowser(URL)
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun updateAllUsers() {
        val users = ArrayList<User>()
        val cursor = allUsers
        var pos = 0
        while (cursor.moveToNext()) {
            val user = User(cursor)
            user.position = pos
            users.add(user)
            pos++
        }

        if (!users.isEmpty())
            updateBalance(*users.toTypedArray())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null) {
            newUser(result.contents)
        } else {
            when (requestCode) {
                USER_OPTIONS_CODE -> {
                    val user : User = getUserFromIntent(data)
                    when (resultCode) {
                        0 -> updateBalance(user)
                        1 -> DeleteUserDialog.newInstance(this, DELETE_USER_CODE, user)
                                .show(fragmentManager, "DeleteUserDialog")
                        2 -> copyCardToClipboard(user.card)
                        3 -> startActivity(BarcodeActivity.intent(context!!, user))
                        4 -> SetNameDialog.newInstance(this, SET_NAME_CODE, user)
                                .show(fragmentManager, "SetNameDialog")
                        else -> {
                        }
                    }
                }
                DELETE_USER_CODE -> {
                    deleteUser(getUserFromIntent(data))
                }
                SET_NAME_CODE -> {
                    updateNameOfUser(getUserFromIntent(data))
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

    override fun onResume() {
        super.onResume()
        requireActivity().setTitle(R.string.navigation_balance)
    }

    override fun onStop() {
        super.onStop()
        hideKeyboard()
    }

    private fun hideKeyboard() {
        val view = requireActivity().currentFocus
        if (view != null) {
            val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
        newUserInput.clearFocus()
    }

    private fun newUser(card: String) {
        when {
            card.length < 15 -> {
                mRootView.snack(context, R.string.balance_new_user_dumb, SnackType.FINISH)
            }
            context.isOnline() -> {
                mRootView.snack(context, getNewUserMsg(card), SnackType.LOADING)
                DownloadUserAsyncTask {resultCode, list ->  onUsersDownloaded(resultCode, list) }
                        .execute(User(card))
            }
            else -> {
                mRootView.snack(context, R.string.no_connection, SnackType.ERROR)
            }
        }
    }

    private fun getNewUserMsg(card: String): String {
        return getString(R.string.balance_new_user_adding).format(card)
    }

    private fun updateBalance(vararg users: User) {
        if (context.isOnline()) {
            context?.let { mRootView.snack(it, R.string.updating, SnackType.LOADING) }

            showProgressBar(*users, show = true)

            DownloadUserAsyncTask { code, list ->  onUsersDownloaded(code, list) }
                    .execute(*users)
        } else {
            mRootView.snack(context, R.string.no_connection, SnackType.ERROR)
        }
    }

    private fun onUsersDownloaded(code: ReturnCode, users: List<User>) {
        when (code) {
            ReturnCode.OK -> {
                updateUser(users)
                newUserInput.text.clear()
            }
            else -> context?.let { mRootView.snack(it, code) }
        }
        showProgressBar(*users.toTypedArray(), show = false)
    }

    private fun updateUser(users: List<User>) {
        var rows = -1

        for (u in users) {
            rows = saveUserBalance(u)

            // Si una fila fue afectada, entonces se actualizó el balance del usuario
            // sinó, insertar el nuevo usuario
            if (rows == 0) {
                insertUser(u)
            }
        }

        if (rows == 1) {
            mRootView.snack(context, R.string.update_success, SnackType.FINISH)
        } else if (rows == 0) {
            mRootView.snack(context, R.string.new_user_success, SnackType.FINISH)
        }
    }

    private fun insertUser(user: User) {
        mContentResolver.insert(UserProvider.CONTENT_URI, user.toContentValues(true))
    }

    private fun saveUserBalance(user: User): Int {
        return mContentResolver.update(
                UserProvider.CONTENT_URI,
                user.toContentValues(false),
                UsersContract.UserEntry.CARD + "=?",
                arrayOf(user.card)
        )
    }

    private fun copyCardToClipboard(userCard: String?) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("Card", userCard)
        mRootView.snack(context, R.string.balance_copy_msg, SnackType.FINISH)
    }

    private fun deleteUser(user: User) {
        mContentResolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                null, null)
        context?.deleteFileInStorage(BARCODE_PATH + user.card)
        mRootView.snack(context, R.string.balance_delete_user_msg, SnackType.FINISH)
    }

    private fun updateNameOfUser(user: User) {
        val values = ContentValues()
        values.put(UsersContract.UserEntry.NAME, user.name)
        mContentResolver.update(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                values, null, null
        )
        mRootView.snack(context, R.string.update_success, SnackType.FINISH)
    }

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return CursorLoader(requireActivity().applicationContext, UserProvider.CONTENT_URI,
                null, null, null, null)
    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor) {
        mUserCursorAdapter.setCursor(data)
        mUserCursorAdapter.notifyDataSetChanged()
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {
        mUserCursorAdapter.setCursor(null)
        mUserCursorAdapter.notifyDataSetChanged()
    }

    private fun showProgressBar(vararg users: User, show: Boolean) {
        for (u in users) {
            u.position?.let {
                mUserCursorAdapter.setInProgress(it, show)
                mUserCursorAdapter.notifyItemChanged(it)
            }
        }
    }

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/autoconsulta.php"
        private const val USER_OPTIONS_CODE = 1
        private const val SET_NAME_CODE = 2
        private const val DELETE_USER_CODE = 3
        const val BARCODE_PATH = "barcode-land-"
    }
}