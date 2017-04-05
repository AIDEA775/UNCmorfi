package com.uncmorfi.balance;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.uncmorfi.R;
import com.uncmorfi.balance.dialogs.NewUserDialog;
import com.uncmorfi.balance.dialogs.UserOptionsDialog;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.balance.model.UsersContract;
import com.uncmorfi.helpers.ConnectionHelper;


public class BalanceFragment extends Fragment implements UserCursorAdapter.OnCardClickListener,
        DownloadUserAsyncTask.DownloadUserListener, LoaderManager.LoaderCallbacks<Cursor> {
    private static final int BARCODE_REQUEST_CODE = 1;
    private static final int NEW_USER_REQUEST_CODE = 2;
    private static final int REFRESH_USER_REQUEST_CODE = 3;
    public static final int UPDATE_REQUEST_CODE = 4;

    private ContentResolver mContentResolver;
    private UserCursorAdapter mUserCursorAdapter;
    private View mRootView;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_balance, container, false);

        mRootView = view;
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.user_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mUserCursorAdapter = new UserCursorAdapter(getContext(), this);
        recyclerView.setAdapter(mUserCursorAdapter);

        mContentResolver = getActivity().getContentResolver();

        getLoaderManager().initLoader(0, null, this);

        return view;
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
    public void onClick(UserCursorAdapter.UserViewHolder holder, int id) {
        final ContentResolver cr = getActivity().getContentResolver();

        Cursor c = cr.query(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, id),
                null,
                null,
                null,
                null);

        if (c != null && c.moveToFirst()) {
            final User user = new User(c);
            c.close();

            UserOptionsDialog userOptions = UserOptionsDialog.newInstance(user, holder);
            userOptions.setTargetFragment(this, REFRESH_USER_REQUEST_CODE);
            userOptions.show(getFragmentManager(), "UserOptionsDialog");
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BARCODE_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    newUser(data.getStringExtra(BarcodeReaderActivity.ARG_BARCODE_CARD));
                }
                break;
            case NEW_USER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    newUser(data.getStringExtra(NewUserDialog.ARG_CARD));
                }
                break;
            case REFRESH_USER_REQUEST_CODE:
                if (resultCode == Activity.RESULT_OK) {
                    if (ConnectionHelper.isOnline(getContext())) {
                        String card = data.getStringExtra(UserOptionsDialog.ARG_CARD);
                        UserCursorAdapter.UserViewHolder holder = (UserCursorAdapter.UserViewHolder)
                                data.getSerializableExtra(UserOptionsDialog.ARG_HOLDER);

                        new RefreshUserAsyncTask(this, holder).execute(card);
                    } else {
                        showShortSnackBarMsg(R.string.no_connection);
                    }
                }
                break;
            case UPDATE_REQUEST_CODE:
                onDataChanged();
                showShortSnackBarMsg(data.getIntExtra("msg", 0));
                break;
            default:
                break;
        }
    }

    private void newUser(String card) {
        Log.i("BalanceFragment", "New card " + card);
        if (ConnectionHelper.isOnline(getContext())) {
            new DownloadUserAsyncTask(this).execute(card);
        } else {
            showShortSnackBarMsg(R.string.no_connection);
        }
    }

    @Override
    public void onUserDownloaded(User user) {
        mContentResolver.insert(UserProvider.CONTENT_URI, user.toContentValues());

        onDataChanged();
        showShortSnackBarMsg(R.string.new_user_success);
    }

    @Override
    public void onUserRefresh(User user) {
        ContentValues values = new ContentValues();
        values.put(UsersContract.UserEntry.BALANCE, user.getBalance());

        mContentResolver.update(
                UserProvider.CONTENT_URI,
                values,
                UsersContract.UserEntry.CARD + "=?",
                new String[]{user.getCard()}
        );
        onDataChanged();
        showShortSnackBarMsg(R.string.refresh_success);
    }

    @Override
    public void onUserDownloadFail() {
        if (isAdded()) {
            Snackbar.make(mRootView, R.string.new_user_fail, Snackbar.LENGTH_LONG)
                    .show();
        }
    }

    private void onDataChanged() {
        mUserCursorAdapter.swapCursor(mContentResolver.query(
                UserProvider.CONTENT_URI,
                null,
                null,
                null,
                null,
                null));
    }

    private void showShortSnackBarMsg(int resId) {
        if (getActivity() != null && isAdded() && resId != 0) {
            Snackbar.make(mRootView, resId, Snackbar.LENGTH_SHORT)
                    .show();
        }
    }

    private class RefreshUserAsyncTask extends DownloadUserAsyncTask {
        private ProgressBar progressBar;

        RefreshUserAsyncTask(DownloadUserListener listener, UserCursorAdapter.UserViewHolder holder) {
            super(listener);
            progressBar = holder.progressBar;
        }

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(User user) {
            progressBar.setVisibility(View.GONE);
            if (user != null) mListener.onUserRefresh(user);
            else mListener.onUserDownloadFail();
        }
    }
}