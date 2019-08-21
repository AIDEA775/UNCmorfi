package com.uncmorfi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uncmorfi.helpers.*
import com.uncmorfi.helpers.StatusCode.*
import com.uncmorfi.models.AppDatabase
import com.uncmorfi.models.DayMenu
import com.uncmorfi.models.Serving
import com.uncmorfi.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*

class MainViewModel(val context: Application): AndroidViewModel(context) {
    private val db: AppDatabase = AppDatabase(context)
    private val userLive: MutableLiveData<List<User>> = MutableLiveData()
    val userStatus: MutableLiveData<StatusCode> = MutableLiveData()

    private val menuLive: MutableLiveData<List<DayMenu>> = MutableLiveData()
    val menuStatus: MutableLiveData<StatusCode> = MutableLiveData()

    private val servingLive: MutableLiveData<List<Serving>> = MutableLiveData()
    val servingStatus: MutableLiveData<StatusCode> = MutableLiveData()

    init {
        userStatus.value = BUSY
        menuStatus.value = BUSY
        servingStatus.value = BUSY
    }

    /*
     * Balance stuff
     */

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
            if (context.isOnline()) {
                val status = withContext(coroutineContext + Dispatchers.IO) {
                    downloadUsersTask(*users)
                }
                usersNotify(status)
            } else {
                usersNotify(NO_CONNECTION)
            }
        }
    }

    fun updateUserName(user: User) {
        viewModelScope.launch(Dispatchers.Main) {
            db.userDao().updateFullUser(user)
            usersNotify(UPDATED)
        }
    }

    fun deleteUser(user: User) {
        viewModelScope.launch(Dispatchers.Main) {
            db.userDao().delete(user)
            // fixme eliminar tambien la caché del codigo de barras de la tarjeta
            usersNotify(DELETED)
        }
    }

    private suspend fun usersNotify(status: StatusCode) {
        userLive.value = db.userDao().getAll()
        userStatus.value = status
    }



    private suspend fun downloadUsersTask(vararg users: User): StatusCode {
        try {
            val cards = users.joinToString(separator = ",") { it.card }
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

    /*
     * Menu stuff
     */

    fun getMenu(): LiveData<List<DayMenu>> {
        if (menuLive.value == null) {
            viewModelScope.launch(Dispatchers.Main) {
                menuLive.value = db.menuDao().getAll()
                menuStatus.value = BUSY
                if (needAutoUpdateMenu()) {
                    updateMenu()
                }
            }
        }
        return menuLive
    }

    fun updateMenu() {
        viewModelScope.launch(Dispatchers.Main) {
            val status = withContext(coroutineContext + Dispatchers.IO) {
                downloadMenuTask()
            }
            menuLive.value = db.menuDao().getAll()
            menuStatus.value = status
        }
    }

    private suspend fun needAutoUpdateMenu(): Boolean {
        val now = Calendar.getInstance()
        now.time = Date()
        val nowWeek = now.get(Calendar.WEEK_OF_YEAR)
        val nowYear = now.get(Calendar.YEAR)

        val menu = Calendar.getInstance()
        menu.time = db.menuDao().getAll().firstOrNull()?.date ?: Date(0) // Date(0) es 1970
        val menuWeek = menu.get(Calendar.WEEK_OF_YEAR)
        val menuYear = now.get(Calendar.YEAR)

        return menuYear < nowYear || menuWeek < nowWeek
    }

    private suspend fun downloadMenuTask(): StatusCode {
        val menuList = mutableListOf<DayMenu>()

        try {
            val result = URL(MENU_URL).downloadByGet()
            val week = JSONObject(result).getJSONObject("menu")

            val keys = week.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String
                val foods = week.getJSONArray(key)
                val day = key.toDate()
                day?.let {
                    menuList.add(DayMenu(day, foods.toArray().toList()))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECTION_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
            return INTERNAL_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (menuList.isEmpty()) {
            return EMPTY_ERROR
        }

        Collections.sort(menuList, ParserHelper.MenuDayComparator())
        val inserts = db.menuDao().insert(*menuList.toTypedArray())

        return if (inserts.all { it == -1L }) OK else UPDATED
    }

    /*
     * Serving stuff
     */

    fun getServings(): LiveData<List<Serving>> {
        if (servingLive.value == null) {
            viewModelScope.launch(Dispatchers.Main) {
                servingLive.value = db.servingDao().getAll()
            }
        }
        return servingLive
    }

    fun updateServings() {
        viewModelScope.launch(Dispatchers.Main) {
            val status = withContext(coroutineContext + Dispatchers.IO) {
                downloadServingsTask()
            }
            servingLive.value = db.servingDao().getAll()
            servingStatus.value = status
        }
    }

    private suspend fun downloadServingsTask(): StatusCode {
        try {
            val result = URL(COUNTER_URL).downloadByGet()
            val items = JSONObject(result).getJSONObject("servings")

            val data = mutableListOf<Serving>()

            val keys = items.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String

                val date = key.toDate("UTC")
                val ration = items.getInt(key)

                date?.let {
                    data.add(Serving(date, ration))
                }
            }
            Collections.sort(data, ParserHelper.ServingsComparator())

            if (data.isEmpty()) {
                return EMPTY_ERROR
            }

            db.servingDao().insert(*data.toTypedArray())
            return UPDATED
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECTION_ERROR
        } catch (e: JSONException) {
            return INTERNAL_ERROR
        } catch (e: NumberFormatException) {
            return INTERNAL_ERROR
        }
    }

    companion object {
        private const val USER_URL = "http://uncmorfi.georgealegre.com/users?codes="
        private const val MENU_URL = "http://uncmorfi.georgealegre.com/menu"
        private const val COUNTER_URL = "http://uncmorfi.georgealegre.com/servings"
    }

}