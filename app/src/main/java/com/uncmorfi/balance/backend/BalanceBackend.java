package com.uncmorfi.balance.backend;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.uncmorfi.R;
import com.uncmorfi.balance.model.User;
import com.uncmorfi.balance.model.UserProvider;
import com.uncmorfi.balance.model.UsersContract;
import com.uncmorfi.helpers.ConnectionHelper;
import com.uncmorfi.helpers.MemoryHelper;
import com.uncmorfi.helpers.SnackbarHelper.SnackType;

import java.util.List;
import java.util.Locale;

/**
 * Se encarga del manejo de datos de los usuarios/tarjetas.
 * Acá están todas las acciones que se pueden hacer sobre los usuarios.
 * Se comunica con la base de datos a través de un {@link ContentResolver}.
 * Un fragmento o actividad debería implementar {@link BalanceListener}.
 * Usa a {@link DownloadUserAsyncTask} para descargar los datos de los usuarios.
 */
public class BalanceBackend implements DownloadUserAsyncTask.DownloadUserListener {
    private static BalanceBackend mInstance;
    public static final String BARCODE_PATH = "barcode-land-";
    private BalanceListener mFragment;
    private Context mContext;
    private ContentResolver mContentResolver;

    public interface BalanceListener {
        void showSnackBarMsg(int resId, SnackType type);
        void showSnackBarMsg(String msg, SnackType type);
        void showProgressBar(int[] position, boolean show);
        void onItemAdded(Cursor c);
        void onItemChanged(int position, Cursor c);
        void onItemDeleted(int position, Cursor c);
    }

    private BalanceBackend(Context context) {
        mContext = context.getApplicationContext();
        mContentResolver = context.getContentResolver();
    }

    public static synchronized BalanceBackend getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new BalanceBackend(context);
        }
        return mInstance;
    }

    public void setListener(BalanceListener listener) {
        mFragment = listener;
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
        Log.i("Backend", "New card: " + card);
        if (card.length() < 15) {
            mFragment.showSnackBarMsg(R.string.balance_new_user_dumb, SnackType.FINISH);
        }else if (ConnectionHelper.isOnline(mContext)) {
            mFragment.showSnackBarMsg(getNewUserMsg(card), SnackType.LOADING);
            new DownloadUserAsyncTask(this, null).execute(card);
        } else {
            mFragment.showSnackBarMsg(R.string.no_connection, SnackType.ERROR);
        }
    }

    public void updateBalanceOfUser(String cards, final int[] positions) {
        Log.i("Backend", "Updating cards: " + cards);
        if (ConnectionHelper.isOnline(mContext)) {
            mFragment.showSnackBarMsg(R.string.updating, SnackType.LOADING);
            mFragment.showProgressBar(positions, true);

            new DownloadUserAsyncTask(this, positions).execute(cards);
        } else {
            mFragment.showSnackBarMsg(R.string.no_connection, SnackType.ERROR);
        }
    }

    public void deleteUser(User user) {
        mContentResolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.getId()),
                null,
                null);
        MemoryHelper.deleteFileInStorage(mContext, BARCODE_PATH + user.getCard());
        mFragment.onItemDeleted(user.getPosition(), getAllUsers());
        mFragment.showSnackBarMsg(R.string.balance_delete_user_msg, SnackType.FINISH);
    }

    public void updateNameOfUser(User user, String name) {
        ContentValues values = new ContentValues();
        values.put(UsersContract.UserEntry.NAME, name);
        mContentResolver.update(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.getId()),
                values,
                null,
                null
        );
        mFragment.onItemChanged(user.getPosition(), getAllUsers());
        mFragment.showSnackBarMsg(R.string.update_success, SnackType.FINISH);
    }

    public void copyCardToClipboard(String userCard) {
        ClipboardManager clipboard = (ClipboardManager)
                mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboard.setPrimaryClip(ClipData.newPlainText("Card", userCard));

        mFragment.showSnackBarMsg(R.string.balance_copy_msg, SnackType.FINISH);
    }

    @Override
    public void onUsersDownloaded(@NonNull List<? extends User> users) {
        int rows = -1;

        for (User u : users) {
             rows = updateUserBalance(u);

            // Si una fila fue afectada, entonces se actualizó el balance del usuario
            // sinó, insertar el nuevo usuario
            if (rows == 1) {
                mFragment.onItemChanged(u.getPosition(), getAllUsers());
                mFragment.showProgressBar(new int[]{u.getPosition()}, false);
            } else if (rows == 0) {
                insertUser(u);
                mFragment.onItemAdded(getAllUsers());
            }
        }

        if (rows == 1) {
            mFragment.showSnackBarMsg(R.string.update_success, SnackType.FINISH);
        } else if (rows == 0) {
            mFragment.showSnackBarMsg(R.string.new_user_success, SnackType.FINISH);
        }
    }

    @Override
    public void onUsersDownloadFail(int code, int[] positions) {
        showError(code);
        if (positions != null)
            mFragment.showProgressBar(positions, false);
    }

    private void showError(int code) {
        switch (code) {
            case ConnectionHelper.CONNECTION_ERROR:
                mFragment.showSnackBarMsg(R.string.connection_error, SnackType.ERROR);
                break;
            case ConnectionHelper.INTERNAL_ERROR:
                mFragment.showSnackBarMsg(R.string.internal_error, SnackType.ERROR);
                break;
            default:
                mFragment.showSnackBarMsg(R.string.internal_error, SnackType.ERROR);
                break;
        }
    }

    private void insertUser(User user) {
        mContentResolver.insert(UserProvider.CONTENT_URI, user.toContentValues(true));
    }

    private int updateUserBalance(User user) {
        return mContentResolver.update(
                UserProvider.CONTENT_URI,
                user.toContentValues(false),
                UsersContract.UserEntry.CARD + "=?",
                new String[]{user.getCard()}
        );
    }

    private Cursor getAllUsers() {
        return mContentResolver.query(
                UserProvider.CONTENT_URI,
                null,
                null,
                null,
                null,
                null);
    }

}