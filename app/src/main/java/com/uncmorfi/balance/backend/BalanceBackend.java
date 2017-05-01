package com.uncmorfi.balance.backend;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.balance.model.UsersContract;
import com.uncmorfi.helpers.SnackbarHelper;
import com.uncmorfi.helpers.ConnectionHelper;

import java.io.Serializable;
import java.util.Locale;


public class BalanceBackend implements DownloadUserAsyncTask.DownloadUserListener, Serializable{
    private BalanceListener mFragment;
    private Context mContext;
    private ContentResolver mContentResolver;


    public interface BalanceListener {
        void showSnackBarMsg(int resId, SnackbarHelper.SnackType type);
        void showSnackBarMsg(String msg, SnackbarHelper.SnackType type);
        void onDataChanged(Cursor c);
        void showProgressBar(int position, boolean show);
    }

    public BalanceBackend(BalanceListener listener, Context context) {
        mFragment = listener;
        mContext = context;
        mContentResolver = context.getContentResolver();
    }

    public User getUserById(int id) {
        User result = null;

        Cursor c = mContentResolver.query(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, id),
                null,
                null,
                null,
                null);

        if (c != null && c.moveToFirst()) {
            result = new User(c);
            c.close();
        }
        return result;
    }

    private String getNewUserMsg(String card) {
        return String.format(Locale.US, mContext.getString(R.string.balance_new_user_adding), card);
    }

    public void newUser(String card) {
        Log.i("Backend", "New card " + card);
        if (ConnectionHelper.isOnline(mContext)) {
            mFragment.showSnackBarMsg(getNewUserMsg(card), SnackbarHelper.SnackType.LOADING);
            new DownloadUserAsyncTask(this).execute(card);
        } else {
            mFragment.showSnackBarMsg(R.string.no_connection, SnackbarHelper.SnackType.ERROR);
        }
    }

    public void updateBalanceOfUser(int userId, final int position) {
        if (ConnectionHelper.isOnline(mContext)) {
            mFragment.showSnackBarMsg(R.string.updating, SnackbarHelper.SnackType.LOADING);

            new DownloadUserAsyncTask(this) {
                @Override
                public void onPreExecute() {
                    super.onPreExecute();
                    mFragment.showProgressBar(position, true);
                }

                @Override
                public void onPostExecute(User user) {
                    mFragment.showProgressBar(position, false);
                    super.onPostExecute(user);
                }
            }.execute(getUserById(userId).getCard());
        } else {
            mFragment.showSnackBarMsg(R.string.no_connection, SnackbarHelper.SnackType.ERROR);
        }
    }

    public void deleteUser(int userId) {
        mContentResolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, userId),
                null,
                null);
        mFragment.onDataChanged(getAllCards());
        mFragment.showSnackBarMsg(R.string.balance_delete_user_msg, SnackbarHelper.SnackType.FINISH);
    }

    public void updateNameOfUser(int userId, String name) {
        ContentValues values = new ContentValues();
        values.put(UsersContract.UserEntry.NAME, name);
        mContentResolver.update(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, userId),
                values,
                null,
                null
        );
        mFragment.onDataChanged(getAllCards());
        mFragment.showSnackBarMsg(R.string.update_success, SnackbarHelper.SnackType.FINISH);
    }

    @Override
    public void onUserDownloaded(User user) {
        int rows = updateUserBalance(user);

        // Si una fila fue afectada, entonces se actualizó el balance del usuario
        // sinó, insertar el nuevo usuario
        if (rows == 1) {
            mFragment.showSnackBarMsg(R.string.update_success, SnackbarHelper.SnackType.FINISH);
        } else {
            insertUser(user);
            mFragment.showSnackBarMsg(R.string.new_user_success, SnackbarHelper.SnackType.FINISH);
        }
        mFragment.onDataChanged(getAllCards());
    }

    @Override
    public void onUserDownloadFail() {
        mFragment.showSnackBarMsg(R.string.new_user_fail, SnackbarHelper.SnackType.ERROR);
    }

    private void insertUser(User user) {
        mContentResolver.insert(UserProvider.CONTENT_URI, user.toContentValues());
    }

    private int updateUserBalance(User user) {
        ContentValues values = new ContentValues();
        values.put(UsersContract.UserEntry.BALANCE, user.getBalance());

        return mContentResolver.update(
                UserProvider.CONTENT_URI,
                values,
                UsersContract.UserEntry.CARD + "=?",
                new String[]{user.getCard()}
        );
    }

    private Cursor getAllCards() {
        return mContentResolver.query(
                UserProvider.CONTENT_URI,
                null,
                null,
                null,
                null,
                null);
    }
}