package com.uncmorfi.balance

import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
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
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.MemoryHelper
import com.uncmorfi.helpers.SnackbarHelper.SnackType
import com.uncmorfi.helpers.SnackbarHelper.showSnack
import kotlinx.android.synthetic.main.fragment_balance.*
import kotlinx.android.synthetic.main.user_new.*
import java.util.*

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
class BalanceFragment : Fragment(), LoaderManager.LoaderCallbacks<Cursor>,
        DownloadUserAsyncTask.DownloadUserListener {
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
        mRootView = inflater.inflate(R.layout.fragment_balance, container, false)
        loaderManager.initLoader(0, null, this)
        return mRootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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

        mUserCursorAdapter = UserCursorAdapter(requireContext(), {
            UserOptionsDialog.newInstance(this, USER_OPTIONS_CODE, it)
                        .show(fragmentManager, "UserOptionsDialog")},
                { updateBalanceOfUser(it.card, intArrayOf(it.position));true })

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

        newUserInput.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Cuando hay texto, cambia la función del botón por newUser.
                if (s.isNotEmpty()) {
                    newUserScanner.apply {
                        setImageResource(R.drawable.ic_check)
                        contentDescription = getString(R.string.balance_new_user_button_enter)
                        setOnClickListener {
                            hideKeyboard()
                            callNewUser()
                        }
                    }
                } else {
                    newUserScanner.apply {
                        setImageResource(R.drawable.ic_barcode)
                        contentDescription = getString(R.string.balance_new_user_button_code)
                        setOnClickListener { callScanner() }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        // Por defecto llama al lector
        newUserScanner.setOnClickListener { callScanner() }
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
            R.id.balance_update -> {
                updateAllUsers()
                true
            }
            R.id.balance_browser -> {
                val i = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
                startActivity(i)
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun updateAllUsers() {
        var cards = ""
        val positions = IntArray(mUserCursorAdapter.itemCount)

        for (pos in 0 until mUserCursorAdapter.itemCount) {
            val userCard = mUserCursorAdapter.getItemCardFromCursor(pos)
            cards += (if (pos == 0) "" else ",") + userCard
            positions[pos] = pos
        }
        if (!cards.isEmpty())
            updateBalanceOfUser(cards, positions)
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
                        0 -> updateBalanceOfUser(user.card, intArrayOf(user.position))
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

    private fun onItemAdded() {
        newUserInput.text.clear() // se agregó la tarjeta, limpiar el EditText.
        mUserCursorAdapter.setCursor(allUsers)
        mUserCursorAdapter.notifyItemInserted(mUserCursorAdapter.itemCount)
    }

    private fun onItemChanged(position: Int) {
        newUserInput.text.clear() // tambien limpiar el EditText por las dudas.
        mUserCursorAdapter.setCursor(allUsers)
        mUserCursorAdapter.notifyItemChanged(position)
    }

    private fun onItemDeleted(position: Int) {
        mUserCursorAdapter.setCursor(allUsers)
        mUserCursorAdapter.notifyItemRemoved(position)

    }

    private fun showSnackBarMsg(resId: Int, type: SnackType) {
        if (activity != null && isAdded && resId != 0)
            showSnack(requireContext(), mRootView, resId, type)
    }

    private fun showSnackBarMsg(msg: String, type: SnackType) {
        if (activity != null && isAdded)
            showSnack(requireContext(), mRootView, msg, type)
    }

    private fun showProgressBar(positions: IntArray, show: Boolean) {
        for (i in positions) {
            mUserCursorAdapter.setInProgress(i, show)
            mUserCursorAdapter.notifyItemChanged(i)
        }
    }

    private fun newUser(card: String) {
        Log.i("Backend", "New card: $card")
        when {
            card.length < 15 -> {
                showSnackBarMsg(R.string.balance_new_user_dumb, SnackType.FINISH)
            }
            ConnectionHelper.isOnline(context) -> {
                showSnackBarMsg(getNewUserMsg(card), SnackType.LOADING)
                DownloadUserAsyncTask(this, null).execute(card)
            }
            else -> {
                showSnackBarMsg(R.string.no_connection, SnackType.ERROR)
            }
        }
    }

    private fun getNewUserMsg(card: String): String {
        return String.format(Locale.US, getString(R.string.balance_new_user_adding), card)
    }

    private fun updateBalanceOfUser(cards: String?, positions: IntArray) {
        Log.i("Backend", "Updating cards: $cards")
        if (ConnectionHelper.isOnline(context)) {
            showSnackBarMsg(R.string.updating, SnackType.LOADING)
            showProgressBar(positions, true)

            DownloadUserAsyncTask(this, positions).execute(cards)
        } else {
            showSnackBarMsg(R.string.no_connection, SnackType.ERROR)
        }
    }

    override fun onUsersDownloaded(users: List<User>) {
        var rows = -1

        for (u in users) {
            rows = updateUserBalance(u)

            // Si una fila fue afectada, entonces se actualizó el balance del usuario
            // sinó, insertar el nuevo usuario
            if (rows == 1) {
                onItemChanged(u.position)
                showProgressBar(intArrayOf(u.position), false)
            } else if (rows == 0) {
                insertUser(u)
                onItemAdded()
            }
        }

        if (rows == 1) {
            showSnackBarMsg(R.string.update_success, SnackType.FINISH)
        } else if (rows == 0) {
            showSnackBarMsg(R.string.new_user_success, SnackType.FINISH)
        }
    }

    override fun onUsersDownloadFail(errorCode: Int, positions: IntArray?) {
        showError(errorCode)
        if (positions != null)
            showProgressBar(positions, false)
    }

    private fun insertUser(user: User) {
        mContentResolver.insert(UserProvider.CONTENT_URI, user.toContentValues(true))
    }

    private fun updateUserBalance(user: User): Int {
        return mContentResolver.update(
                UserProvider.CONTENT_URI,
                user.toContentValues(false),
                UsersContract.UserEntry.CARD + "=?",
                arrayOf(user.card ?: "")
        )
    }

    private fun copyCardToClipboard(userCard: String?) {
        val clipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("Card", userCard)

        showSnackBarMsg(R.string.balance_copy_msg, SnackType.FINISH)
    }

    private fun deleteUser(user: User) {
        mContentResolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                null, null)
        MemoryHelper.deleteFileInStorage(context!!, BARCODE_PATH + user.card)
        onItemDeleted(user.position)
        showSnackBarMsg(R.string.balance_delete_user_msg, SnackType.FINISH)
    }

    private fun updateNameOfUser(user: User) {
        val values = ContentValues()
        values.put(UsersContract.UserEntry.NAME, user.name)
        mContentResolver.update(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                values, null, null
        )
        onItemChanged(user.position)
        showSnackBarMsg(R.string.update_success, SnackType.FINISH)
    }

    private fun showError(code: Int) {
        when (code) {
            ConnectionHelper.CONNECTION_ERROR -> {
                showSnackBarMsg(R.string.connection_error, SnackType.ERROR)
            }
            ConnectionHelper.INTERNAL_ERROR -> {
                showSnackBarMsg(R.string.internal_error, SnackType.ERROR)
            }
            else -> showSnackBarMsg(R.string.internal_error, SnackType.ERROR)
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