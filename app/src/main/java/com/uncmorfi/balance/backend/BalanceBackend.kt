package com.uncmorfi.balance.backend

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log

import com.uncmorfi.R
import com.uncmorfi.balance.model.User
import com.uncmorfi.balance.model.UserProvider
import com.uncmorfi.balance.model.UsersContract
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.MemoryHelper
import com.uncmorfi.helpers.SnackbarHelper.SnackType
import java.util.Locale

/**
 * Se encarga del manejo de datos de los usuarios/tarjetas.
 * Acá están todas las acciones que se pueden hacer sobre los usuarios.
 * Se comunica con la base de datos a través de un [ContentResolver].
 * Un fragmento o actividad debería implementar [BalanceListener].
 * Usa a [DownloadUserAsyncTask] para descargar los datos de los usuarios.
 */
class BalanceBackend private constructor(context: Context) :
        DownloadUserAsyncTask.DownloadUserListener {
    private lateinit var mFragment: BalanceListener
    private val mContext: Context = context.applicationContext
    private val mContentResolver: ContentResolver = context.contentResolver

    private val allUsers: Cursor
        get() = mContentResolver.query(
                UserProvider.CONTENT_URI,
                null, null, null, null, null)

    interface BalanceListener {
        fun showSnackBarMsg(resId: Int, type: SnackType)
        fun showSnackBarMsg(msg: String, type: SnackType)
        fun showProgressBar(positions: IntArray, show: Boolean)
        fun onItemAdded(c: Cursor)
        fun onItemChanged(position: Int, c: Cursor)
        fun onItemDeleted(position: Int, c: Cursor)
    }

    fun setListener(listener: BalanceListener) {
        mFragment = listener
    }

    fun getUserById(id: Int): User? {
        var result: User? = null

        val c = mContentResolver.query(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, id.toLong()),
                null, null, null, null)

        if (c != null && c.moveToFirst()) {
            result = User(c)
            c.close()
        }
        return result
    }

    private fun getNewUserMsg(card: String): String {
        return String.format(Locale.US, mContext.getString(R.string.balance_new_user_adding), card)
    }

    fun newUser(card: String) {
        Log.i("Backend", "New card: $card")
        when {
            card.length < 15 -> {
                mFragment.showSnackBarMsg(R.string.balance_new_user_dumb, SnackType.FINISH)
            }
            ConnectionHelper.isOnline(mContext) -> {
                mFragment.showSnackBarMsg(getNewUserMsg(card), SnackType.LOADING)
                DownloadUserAsyncTask(this, null).execute(card)
            }
            else -> {
                mFragment.showSnackBarMsg(R.string.no_connection, SnackType.ERROR)
            }
        }
    }

    fun updateBalanceOfUser(cards: String?, positions: IntArray) {
        Log.i("Backend", "Updating cards: $cards")
        if (ConnectionHelper.isOnline(mContext)) {
            mFragment.showSnackBarMsg(R.string.updating, SnackType.LOADING)
            mFragment.showProgressBar(positions, true)

            DownloadUserAsyncTask(this, positions).execute(cards)
        } else {
            mFragment.showSnackBarMsg(R.string.no_connection, SnackType.ERROR)
        }
    }

    fun deleteUser(user: User) {
        mContentResolver.delete(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                null, null)
        MemoryHelper.deleteFileInStorage(mContext, BARCODE_PATH + user.card)
        mFragment.onItemDeleted(user.position, allUsers)
        mFragment.showSnackBarMsg(R.string.balance_delete_user_msg, SnackType.FINISH)
    }

    fun updateNameOfUser(user: User, name: String) {
        val values = ContentValues()
        values.put(UsersContract.UserEntry.NAME, name)
        mContentResolver.update(
                ContentUris.withAppendedId(UserProvider.CONTENT_URI, user.id.toLong()),
                values, null, null
        )
        mFragment.onItemChanged(user.position, allUsers)
        mFragment.showSnackBarMsg(R.string.update_success, SnackType.FINISH)
    }

    fun copyCardToClipboard(userCard: String?) {
        val clipboard = mContext.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip = ClipData.newPlainText("Card", userCard)

        mFragment.showSnackBarMsg(R.string.balance_copy_msg, SnackType.FINISH)
    }

    override fun onUsersDownloaded(users: List<User>) {
        var rows = -1

        for (u in users) {
            rows = updateUserBalance(u)

            // Si una fila fue afectada, entonces se actualizó el balance del usuario
            // sinó, insertar el nuevo usuario
            if (rows == 1) {
                mFragment.onItemChanged(u.position, allUsers)
                mFragment.showProgressBar(intArrayOf(u.position), false)
            } else if (rows == 0) {
                insertUser(u)
                mFragment.onItemAdded(allUsers)
            }
        }

        if (rows == 1) {
            mFragment.showSnackBarMsg(R.string.update_success, SnackType.FINISH)
        } else if (rows == 0) {
            mFragment.showSnackBarMsg(R.string.new_user_success, SnackType.FINISH)
        }
    }

    override fun onUsersDownloadFail(errorCode: Int, positions: IntArray?) {
        showError(errorCode)
        if (positions != null)
            mFragment.showProgressBar(positions, false)
    }

    private fun showError(code: Int) {
        when (code) {
            ConnectionHelper.CONNECTION_ERROR -> {
                mFragment.showSnackBarMsg(R.string.connection_error, SnackType.ERROR)
            }
            ConnectionHelper.INTERNAL_ERROR -> {
                mFragment.showSnackBarMsg(R.string.internal_error, SnackType.ERROR)
            }
            else -> mFragment.showSnackBarMsg(R.string.internal_error, SnackType.ERROR)
        }
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

    companion object {
        private var mInstance: BalanceBackend? = null
        const val BARCODE_PATH = "barcode-land-"

        @Synchronized
        private fun createInstance(context: Context): BalanceBackend {
            if (mInstance == null)
                mInstance = BalanceBackend(context)
            return mInstance as BalanceBackend
        }

        fun getInstance(context: Context): BalanceBackend {
            return mInstance ?: createInstance(context)
        }
    }

}