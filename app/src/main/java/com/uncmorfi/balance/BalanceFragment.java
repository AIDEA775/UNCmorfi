package com.uncmorfi.balance;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.InputFilter;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;
import com.uncmorfi.balance.dialogs.UserOptionsDialog;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.helpers.SnackbarHelper.SnackType;

import static com.uncmorfi.helpers.SnackbarHelper.showSnack;

/**
 * Saldo de las tarjetas.
 * Administra toda la UI.
 * Usa a {@link UserCursorAdapter} para llenar el {@link RecyclerView}.
 * Usa a {@link UserOptionsDialog} para que el usuario pueda modificar alguna tarjeta.
 * Usa un {@link BalanceBackend} para el manejo de datos.
 */
public class BalanceFragment extends Fragment implements UserCursorAdapter.OnCardClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, BalanceBackend.BalanceListener {
    private View mRootView;
    private UserCursorAdapter mUserCursorAdapter;
    private BalanceBackend mBackend;
    private EditText mEditText;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_balance, container, false);

        initRecyclerAndAdapter();
        initNewUserView();

        mBackend = new BalanceBackend(getActivity().getApplicationContext(), this);
        getLoaderManager().initLoader(0, null, this);

        return mRootView;
    }

    private void initRecyclerAndAdapter() {
        RecyclerView recyclerView = mRootView.findViewById(R.id.balance_list);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setAutoMeasureEnabled(true);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(layoutManager);
        mUserCursorAdapter = new UserCursorAdapter(getContext(), this);
        recyclerView.setAdapter(mUserCursorAdapter);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void initNewUserView() {
        mEditText = mRootView.findViewById(R.id.new_user_input);
        ImageButton scanner = mRootView.findViewById(R.id.new_user_scanner);

        mEditText.setFilters(new InputFilter[] {new InputFilter.AllCaps()});
        mEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i == EditorInfo.IME_ACTION_DONE) {
                    hideKeyboard();
                    mBackend.newUser(textView.getText().toString().replace(" ", ","));
                }
                return false;
            }
        });
        scanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callScanner();
            }
        });
    }

    /**
     * Inicia el lector de barras.
     * Devuelve el resultado por {@link #onActivityResult(int, int, Intent)}.
     */
    private void callScanner() {
        IntentIntegrator integrator = IntentIntegrator.forSupportFragment(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES);
        integrator.setPrompt(getString(R.string.balance_align_barcode));
        integrator.setBeepEnabled(false);
        integrator.setBarcodeImageEnabled(true);
        integrator.initiateScan();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.balance, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() ==  R.id.balance_update) {
            updateAllUsers();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateAllUsers() {
        String cards = "";
        int[] positions = new int[mUserCursorAdapter.getItemCount()];

        for (int pos = 0; pos < mUserCursorAdapter.getItemCount(); pos++) {
            String userCard = mUserCursorAdapter.getItemCardFromCursor(pos);
            cards += (pos == 0 ? "" : ",") + userCard;
            positions[pos] = pos;
        }
        if (!cards.isEmpty())
            mBackend.updateBalanceOfUser(cards, positions);
    }

    /**
     * Callback del Adapter cuando se selecciona una tarjeta.
     */
    @Override
    public void onClick(int userId, String userCard, int position) {
        User user = new User();
        user.setId(userId);
        user.setCard(userCard);
        user.setPosition(position);

        UserOptionsDialog
                .newInstance(user, mBackend)
                .show(getFragmentManager(), "UserOptionsDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null && result.getContents() != null)
            mBackend.newUser(result.getContents());
        else
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_balance);
    }

    @Override
    public void onStop() {
        super.onStop();
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = getActivity().getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        mEditText.clearFocus();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(getActivity().getApplicationContext(),
                UserProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mUserCursorAdapter != null) {
            mUserCursorAdapter.setCursor(data);
            mUserCursorAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUserCursorAdapter.setCursor(null);
        mUserCursorAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemAdded(Cursor c) {
        mEditText.getText().clear(); // se agregó la tarjeta, limpiar el EditText.
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemInserted(mUserCursorAdapter.getItemCount());
    }

    @Override
    public void onItemChanged(int position, Cursor c) {
        mEditText.getText().clear(); // tambien limpiar el EditText por las dudas.
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemDeleted(int position, Cursor c) {
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemRemoved(position);

    }

    @Override
    public void showSnackBarMsg(int resId, SnackType type) {
        if (getActivity() != null && isAdded() && resId != 0)
            showSnack(getContext(), mRootView, resId, type);
    }

    @Override
    public void showSnackBarMsg(String msg, SnackType type) {
        if (getActivity() != null && isAdded())
            showSnack(getContext(), mRootView, msg, type);
    }

    @Override
    public void showProgressBar(int[] positions, boolean show) {
        for (int i : positions) {
            mUserCursorAdapter.setInProgress(i, show);
            mUserCursorAdapter.notifyItemChanged(i);
        }
    }

}