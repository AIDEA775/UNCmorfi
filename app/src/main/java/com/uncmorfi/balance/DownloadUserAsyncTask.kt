package com.uncmorfi.balance

import android.os.AsyncTask
import com.uncmorfi.helpers.StatusCode
import com.uncmorfi.helpers.downloadByGet
import com.uncmorfi.helpers.toDate
import com.uncmorfi.models.User
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.util.*

/**
 * Descarga y parsea uno o más usuarios a partir del codigo de la tarjeta dentro de cada User.
 */
internal class DownloadUserAsyncTask (private val mListener: (code: StatusCode, List<User>) -> Unit) :
        AsyncTask<User, Void, List<User>>() {
    private var mErrorCode: StatusCode = StatusCode.OK

    override fun doInBackground(vararg params: User): List<User> {
        val users = params.asList()
        try {
            var cards = ""
            for (pos in 0 until users.size) {
                cards += (if (pos == 0) "" else ",") + users[pos].card
            }
            val result = URL(url + cards).downloadByGet()
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

                    val expireDate = item.getString("expirationDate").toDate()
                    if (expireDate != null) user.expiration = expireDate.time

                    val currentTime = Calendar.getInstance().time
                    user.lastUpdate = currentTime.time
                }
            }
        } catch (e: IOException) {
            mErrorCode = StatusCode.CONNECTION_ERROR
            e.printStackTrace()
        } catch (e: JSONException) {
            mErrorCode = StatusCode.INTERNAL_ERROR
            e.printStackTrace()
        }
        return users
    }

    override fun onPostExecute(result: List<User>) {
        mListener(mErrorCode, result)
    }

    companion object {
        private const val url = "http://uncmorfi.georgealegre.com/users?codes="
    }
}