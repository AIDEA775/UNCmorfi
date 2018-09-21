package com.uncmorfi.balance

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.LoaderManager
import android.support.v4.content.CursorLoader
import android.support.v4.content.Loader
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageButton
import com.google.zxing.integration.android.IntentIntegrator
import com.uncmorfi.R
import com.uncmorfi.balance.backend.BalanceBackend
import com.uncmorfi.balance.dialogs.UserOptionsDialog
import com.uncmorfi.balance.model.UserProvider
import com.uncmorfi.helpers.SnackbarHelper.SnackType
import com.uncmorfi.helpers.SnackbarHelper.showSnack

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a [UserCursorAdapter] para llenar el [RecyclerView].
 * Usa a [UserOptionsDialog] para que el usuario pueda modificar alguna tarjeta.
 * Usa un [BalanceBackend] para el manejo de datos.
 */
class BalanceFragment : Fragment(), UserCursorAdapter.OnCardClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, BalanceBackend.BalanceListener {
    private lateinit var mRootView: View
    private lateinit var mUserCursorAdapter: UserCursorAdapter
    private lateinit var mBackend: BalanceBackend
    private lateinit var mInputCard: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        mRootView = inflater.inflate(R.layout.fragment_balance, container, false)

        initRecyclerAndAdapter()
        initNewUserView()

        mBackend = BalanceBackend.getInstance(requireContext())
        mBackend.setListener(this)

        loaderManager.initLoader(0, null, this)

        return mRootView
    }

    private fun initRecyclerAndAdapter() {
        val recyclerView = mRootView.findViewById<RecyclerView>(R.id.balance_list)
        val layoutManager = object : LinearLayoutManager(context) {
            override fun isAutoMeasureEnabled(): Boolean {
                return true
            }
        }
        recyclerView.isNestedScrollingEnabled = false
        recyclerView.layoutManager = layoutManager
        mUserCursorAdapter = UserCursorAdapter(requireContext(), this)
        recyclerView.adapter = mUserCursorAdapter
        recyclerView.addItemDecoration(
                DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

    private fun initNewUserView() {
        mInputCard = mRootView.findViewById(R.id.new_user_input)
        val inputButton = mRootView.findViewById<ImageButton>(R.id.new_user_scanner)

        mInputCard.filters = arrayOf<InputFilter>(InputFilter.AllCaps())
        mInputCard.setOnEditorActionListener { _, i, _ ->
            if (i == EditorInfo.IME_ACTION_DONE) {
                hideKeyboard()
                callNewUser()
            }
            false
        }

        mInputCard.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Cuando hay texto, cambia la función del botón por newUser.
                if (s.isNotEmpty()) {
                    inputButton.setImageResource(R.drawable.ic_check)
                    inputButton.contentDescription = getString(R.string.balance_new_user_button_enter)
                    inputButton.setOnClickListener {
                        hideKeyboard()
                        callNewUser()
                    }
                } else {
                    inputButton.setImageResource(R.drawable.ic_barcode)
                    inputButton.contentDescription = getString(R.string.balance_new_user_button_code)
                    inputButton.setOnClickListener { callScanner() }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable) {}
        })

        // Por defecto llama al lector
        inputButton.setOnClickListener { callScanner() }
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
        val input = mInputCard.text.toString().replace(" ", ",")
        if (input.isNotEmpty())
            mBackend.newUser(input)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.balance, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.balance_update) {
            updateAllUsers()
            return true
        } else if (item.itemId == R.id.balance_browser) {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(URL))
            startActivity(i)
            return true
        }
        return super.onOptionsItemSelected(item)
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
            mBackend.updateBalanceOfUser(cards, positions)
    }

    /**
     * Callback del Adapter cuando se selecciona una tarjeta.
     */
    override fun onClick(userId: Int, userCard: String?, position: Int) {
        val user = mBackend.getUserById(userId)

        if (user != null) {
            user.position = position

            UserOptionsDialog
                    .newInstance(user)
                    .show(fragmentManager, "UserOptionsDialog")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null && result.contents != null)
            mBackend.newUser(result.contents)
        else
            super.onActivityResult(requestCode, resultCode, data)
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
        mInputCard.clearFocus()
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

    override fun onItemAdded(c: Cursor) {
        mInputCard.text.clear() // se agregó la tarjeta, limpiar el EditText.
        mUserCursorAdapter.setCursor(c)
        mUserCursorAdapter.notifyItemInserted(mUserCursorAdapter.itemCount)
    }

    override fun onItemChanged(position: Int, c: Cursor) {
        mInputCard.text.clear() // tambien limpiar el EditText por las dudas.
        mUserCursorAdapter.setCursor(c)
        mUserCursorAdapter.notifyItemChanged(position)
    }

    override fun onItemDeleted(position: Int, c: Cursor) {
        mUserCursorAdapter.setCursor(c)
        mUserCursorAdapter.notifyItemRemoved(position)

    }

    override fun showSnackBarMsg(resId: Int, type: SnackType) {
        if (activity != null && isAdded && resId != 0)
            showSnack(requireContext(), mRootView, resId, type)
    }

    override fun showSnackBarMsg(msg: String, type: SnackType) {
        if (activity != null && isAdded)
            showSnack(requireContext(), mRootView, msg, type)
    }

    override fun showProgressBar(positions: IntArray, show: Boolean) {
        for (i in positions) {
            mUserCursorAdapter.setInProgress(i, show)
            mUserCursorAdapter.notifyItemChanged(i)
        }
    }

    companion object {
        private const val URL = "http://comedor.unc.edu.ar/autoconsulta.php"
    }
}