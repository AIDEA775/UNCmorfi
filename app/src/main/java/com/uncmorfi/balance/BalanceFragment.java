package com.uncmorfi.balance;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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
import com.uncmorfi.balance.dialogs.NewUserDialog;
import com.uncmorfi.balance.dialogs.UserOptionsDialog;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.helpers.FABHelper;
import com.uncmorfi.helpers.SnackbarHelper;


public class BalanceFragment extends Fragment implements UserCursorAdapter.OnCardClickListener,
        LoaderManager.LoaderCallbacks<Cursor>, BalanceBackend.BalanceListener {
    private static final int BARCODE_REQUEST_CODE = 1;
    private static final int NEW_USER_REQUEST_CODE = 2;

    private UserCursorAdapter mUserCursorAdapter;
    private View mRootView;
    private BalanceBackend mBackend;
    private RecyclerView mRecyclerView;
    private FloatingActionButton mAddFab;
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

        mRecyclerView = (RecyclerView) mRootView.findViewById(R.id.balance_list);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mUserCursorAdapter = new UserCursorAdapter(getContext(), this);
        mRecyclerView.setAdapter(mUserCursorAdapter);
        mAddFab = (FloatingActionButton) mRootView.findViewById(R.id.balance_fab);
        mAddFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayNewUser();
            }
        });
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx,int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0) {
                    // Scroll Down
                    FABHelper.hideFAB(getContext(), mAddFab);
                } else if (dy < 0 && mAddFab.getScaleX() == 0) {
                    // Scroll Up
                    FABHelper.showFAB(getContext(), mAddFab);
                }
            }
        });
        mBackend = new BalanceBackend(this, getContext());

        getLoaderManager().initLoader(0, null, this);

        return mRootView;
    }

    private void displayNewUser() {
        Intent i = new Intent(getActivity(), BarcodeReaderActivity.class);
        startActivityForResult(i, BARCODE_REQUEST_CODE);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.balance, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_new_user:
                NewUserDialog newUser = new NewUserDialog();
                newUser.setTargetFragment(this, NEW_USER_REQUEST_CODE);
                newUser.show(getFragmentManager(), "NewUserDialog");
                break;
            case R.id.action_new_user_camera:
                Intent i = new Intent(getActivity(), BarcodeReaderActivity.class);
                startActivityForResult(i, BARCODE_REQUEST_CODE);
                break;
        }
        return super.onOptionsItemSelected(item);
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
        return new CursorLoader(getActivity(),
                UserProvider.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (mUserCursorAdapter != null) mUserCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mUserCursorAdapter.swapCursor(null);
    }

    @Override
    public void onClick(int position, int userId) {
        UserOptionsDialog userOptions = UserOptionsDialog.newInstance(userId, position, mBackend);
        userOptions.show(getFragmentManager(), "UserOptionsDialog");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BARCODE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK)
                    mBackend.newUser(data.getStringExtra(BarcodeReaderActivity.ARG_BARCODE_CARD));
                break;
            case NEW_USER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK)
                    mBackend.newUser(data.getStringExtra(NewUserDialog.ARG_CARD));
                break;
            default:
                break;
        }
    }

    @Override
    public void onDataChanged(Cursor c) {
        mUserCursorAdapter.swapCursor(c);
    }

    @Override
    public void showSnackBarMsg(int resId, SnackbarHelper.SnackType type) {
        if (getActivity() != null && isAdded() && resId != 0) {
            lastSnackBar = Snackbar.make(mRootView, resId, SnackbarHelper.getLength(type));
            SnackbarHelper.getColored(getContext(), lastSnackBar, type).show();
        }
    }

    @Override
    public void showProgressBar(int position, boolean show) {
        UserCursorAdapter.UserViewHolder holder = (UserCursorAdapter.UserViewHolder)
                mRecyclerView.findViewHolderForAdapterPosition(position);

        if (holder != null) {
            if (show) holder.progressBar.setVisibility(View.VISIBLE);
            else holder.progressBar.setVisibility(View.GONE);
        }
    }
}