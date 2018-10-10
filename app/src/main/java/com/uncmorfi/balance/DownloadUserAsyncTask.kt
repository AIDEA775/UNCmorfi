package com.uncmorfi.balance

import android.os.AsyncTask
import com.uncmorfi.balance.model.User
import com.uncmorfi.helpers.ConnectionHelper
import com.uncmorfi.helpers.ParserHelper
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.util.*

/**
 * Descarga y parsea uno o más usuarios a partir del codigo de la tarjeta dentro de cada User.
 */
internal class DownloadUserAsyncTask (private val mListener: (resultCode: Int, List<User>) -> Unit) :
        AsyncTask<User, Void, List<User>>() {
    private var mErrorCode: Int = 0

    override fun doInBackground(vararg params: User): List<User> {
        val users = params.asList()
        try {
            var cards = ""
            for (pos in 0 until users.size) {
                cards += (if (pos == 0) "" else ",") + users[pos].card
            }
            val result = ConnectionHelper.downloadFromUrlByGet(URL + cards)
            val array = JSONArray(result)

            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)

                val card = item.getString("code")
                val user = users.find { u -> u.card == card }

                if (user != null) {
                    user.name = item.getString("name")
                    user.type = item.getString("type")
                    user.image = item.getString("imageURL")
                    user.balance = item.getInt("balance")

                    val expireDate = ParserHelper.stringToDate(item.getString("expirationDate"))
                    if (expireDate != null) user.expiration = expireDate.time

                    val currentTime = Calendar.getInstance().time
                    user.lastUpdate = currentTime.time
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            mErrorCode = ConnectionHelper.CONNECTION_ERROR
        } catch (e: JSONException) {
            mErrorCode = ConnectionHelper.INTERNAL_ERROR
            e.printStackTrace()
        }
        return users
    }

    override fun onPostExecute(users: List<User>) {
        mListener(mErrorCode, users)
    }

    companion object {
        private const val URL = "http://uncmorfi.georgealegre.com/users?codes="
    }
}