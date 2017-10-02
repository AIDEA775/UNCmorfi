package com.uncmorfi.balance;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.uncmorfi.R;
import com.uncmorfi.balance.backend.BalanceBackend;
import com.uncmorfi.balance.dialogs.UserOptionsDialog;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.helpers.SnackbarHelper;


public class BalanceFragment extends Fragment implements UserCursorAdapter.OnCardClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, BalanceBackend.BalanceListener {
    private static final int NEW_USER_REQUEST_CODE = 1;

    private View mRootView;
    private UserCursorAdapter mUserCursorAdapter;
    private BalanceBackend mBackend;
    private Snackbar lastSnackBar;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_balance, container, false);

        setRecyclerAndAdapter();
        setFloatingActionButton();

        mBackend = new BalanceBackend(getActivity().getApplicationContext(), this);
        getLoaderManager().initLoader(0, null, this);

        return mRootView;
    }

    private void setRecyclerAndAdapter() {
        RecyclerView recyclerView = (RecyclerView) mRootView.findViewById(R.id.balance_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserCursorAdapter = new UserCursorAdapter(getContext(), this);
        recyclerView.setAdapter(mUserCursorAdapter);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
    }

    private void setFloatingActionButton() {
        (mRootView.findViewById(R.id.balance_fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNewUser();
            }
        });
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

    @Override
    public void onClick(int userId, int position) {
        UserOptionsDialog.newInstance(userId, position, mBackend)
                .show(getFragmentManager(), "UserOptionsDialog");
    }

    private void updateAllUsers() {
        for (int pos = 0; pos < mUserCursorAdapter.getItemCount(); pos++) {
            int userId = mUserCursorAdapter.getItemIdFromCursor(pos);
            mBackend.updateBalanceOfUser(userId, pos);
        }
    }

    private void displayNewUser() {
        Intent i = new Intent(getActivity(), BarcodeReaderActivity.class);
        startActivityForResult(i, NEW_USER_REQUEST_CODE);
    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().setTitle(R.string.navigation_balance);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (lastSnackBar != null && lastSnackBar.isShown())
            lastSnackBar.dismiss();
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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == NEW_USER_REQUEST_CODE && resultCode == Activity.RESULT_OK)
            mBackend.newUser(data.getStringExtra(BarcodeReaderActivity.ARG_BARCODE_CARD));
    }

    @Override
    public void onItemAdded(Cursor c) {
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemInserted(mUserCursorAdapter.getItemCount());
    }

    @Override
    public void onItemChanged(int position, Cursor c) {
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemChanged(position);
    }

    @Override
    public void onItemDeleted(int position, Cursor c) {
        mUserCursorAdapter.setCursor(c);
        mUserCursorAdapter.notifyItemRemoved(position);

    }

    @Override
    public void showSnackBarMsg(int resId, SnackbarHelper.SnackType type) {
        if (getActivity() != null && isAdded() && resId != 0) {
            lastSnackBar = Snackbar.make(mRootView, resId, SnackbarHelper.getLength(type));
            SnackbarHelper.getColored(getContext(), lastSnackBar, type).show();
        }
    }

    @Override
    public void showSnackBarMsg(String msg, SnackbarHelper.SnackType type) {
        if (getActivity() != null && isAdded() && msg != null) {
            lastSnackBar = Snackbar.make(mRootView, msg, SnackbarHelper.getLength(type));
            SnackbarHelper.getColored(getContext(), lastSnackBar, type).show();
        }
    }

    @Override
    public void showProgressBar(int position, boolean show) {
        mUserCursorAdapter.setInProgress(position, show);
        mUserCursorAdapter.notifyItemChanged(position);
    }
}