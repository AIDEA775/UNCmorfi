package com.uncmorfi.balance.backend

import android.os.AsyncTask
import com.uncmorfi.balance.model.User
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.ParserHelper
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.*

/**
 * Descarga y parsea uno o más usuarios a partir del codigo de la tarjeta.
 */
internal class DownloadUserAsyncTask
/**
 * @param position Posiciones de usuarios en el [com.uncmorfi.balance.UserCursorAdapter]
 * Necesario para actualizar la interfaz de los item de los usuarios.
 */
(private val mListener: DownloadUserListener, private val mPosition: IntArray?) :
        AsyncTask<String, Void, List<User>>() {
    private var mErrorCode: Int = 0

    internal interface DownloadUserListener {
        fun onUsersDownloaded(users: List<User>)
        fun onUsersDownloadFail(errorCode: Int, positions: IntArray?)
    }

    override fun doInBackground(vararg params: String): List<User>? {
        try {
            val card = params[0]
            val result = ConnectionHelper.downloadFromUrlByGet(URL + card)

            val array = JSONArray(result)
            val users = ArrayList<User>()

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                val user = User()

                user.card = item.getString("code")
                user.name = item.getString("name")
                user.type = item.getString("type")
                user.image = item.getString("imageURL")
                user.balance = item.getInt("balance")

                val expireDate = ParserHelper.stringToDate(item.getString("expirationDate"))
                if (expireDate != null) user.expiration = expireDate.time

                val currentTime = Calendar.getInstance().time
                user.lastUpdate = currentTime.time

                if (mPosition != null) user.position = mPosition[i]

                users.add(user)
            }
            return users
        } catch (e: IOException) {
            mErrorCode = ConnectionHelper.CONNECTION_ERROR
            return null
        } catch (e: JSONException) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR
            e.printStackTrace()
            return null
        }
    }

    override fun onPostExecute(users: List<User>?) {
        if (users == null || users.isEmpty())
            mListener.onUsersDownloadFail(mErrorCode, mPosition)
        else
            mListener.onUsersDownloaded(users)
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/users?codes="
    }
}