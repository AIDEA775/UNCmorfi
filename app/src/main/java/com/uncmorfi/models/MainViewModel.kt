package com.uncmorfi.models

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.uncmorfi.helpers.StatusCode
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.helpers.downloadByGet
import com.uncmorfi.helpers.toDate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import java.io.IOException
import java.net.URL
import java.util.*

class MainViewModel(context: Context): ViewModel() {
    private val db: AppDatabase = AppDatabase(context)
    private val userLive: MutableLiveData<List<User>> = MutableLiveData()
    val userStatus: MutableLiveData<StatusCode> = MutableLiveData()

    init {
        userStatus.value = BUSY
    }

    fun allUsers(): LiveData<List<User>> {
        if (userLive.value == null) {
            viewModelScope.launch(Dispatchers.Main) {
                userLive.value = db.userDao().getAll()
            }
        }
        return userLive
    }

    fun downloadUsers(vararg users: User) {
        viewModelScope.launch(Dispatchers.Main) {
            val status = withContext(coroutineContext + Dispatchers.IO) {
                downloadUsersTask(*users)
            }
            notify(status)
        }
    }

    fun updateUserName(user: User) {
        viewModelScope.launch(Dispatchers.Main) {
            db.userDao().updateFullUser(user)
            notify(UPDATED)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.Main) {
            db.userDao().delete(user)
            notify(DELETED)
        }
    }

    private suspend fun notify(status: StatusCode) {
        userLive.value = db.userDao().getAll()
        userStatus.value = status
    }



    private suspend fun downloadUsersTask(vararg users: User): StatusCode {
        try {
            var cards = ""
            for (pos in 0 until users.size) {
                cards += (if (pos == 0) "" else ",") + users[pos].card
            }
            val result = URL(USER_URL + cards).downloadByGet()
            val array = JSONArray(result)

            if (array.length() == 0) {
                return EMPTY_ERROR
            }

            val userUpdated = mutableListOf<User>()
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
                    userUpdated.add(user)
                }
            }
            val rows = db.userDao().upsertUser(*userUpdated.toTypedArray())
            return if (rows > 0) UPDATED else INSERTED
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECTION_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
            return INTERNAL_ERROR
        }
    }


    companion object {
        private const val USER_URL = "http://uncmorfi.georgealegre.com/users?codes="
    }

}