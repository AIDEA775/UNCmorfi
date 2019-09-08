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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.util.*
import kotlin.coroutines.coroutineContext

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
            mainDispatch {
                userLive.value = db.userDao().getAll()
            }
        }
        return userLive
    }

    fun downloadUsers(vararg users: User) {
        mainDispatch {
            if (context.isOnline()) {
                val status = ioDispatch { downloadUsersTask(*users) }
                usersNotify(status)
            } else {
                usersNotify(NO_ONLINE)
            }
        }
    }

    fun updateUserName(user: User) {
        mainDispatch {
            db.userDao().updateFullUser(user)
            usersNotify(UPDATE_SUCCESS)
        }
    }

    fun deleteUser(user: User) {
        mainDispatch {
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
                return UPDATE_ERROR
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

                    val expireDate = item.getString("expirationDate").toCalendar()
                    if (expireDate != null) user.expiration = expireDate

                    user.lastUpdate = Calendar.getInstance()
                    userUpdated.add(user)
                }
            }
            val rows = db.userDao().upsertUser(*userUpdated.toTypedArray())
            return if (rows > 0) UPDATE_SUCCESS else INSERTED
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECT_ERROR
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
            mainDispatch {
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
        mainDispatch {
            if (context.isOnline()) {
                menuStatus.value = ioDispatch { downloadMenuTask() }
                menuLive.value = db.menuDao().getAll()
            } else {
                menuStatus.value = NO_ONLINE
            }

        }
    }

    fun clearMenu() {
        mainDispatch {
            db.menuDao().clearAll()
            menuLive.value = db.menuDao().getAll()
            updateMenu()
        }
    }

    private suspend fun needAutoUpdateMenu(): Boolean {
        val now = Calendar.getInstance()
        now.time = Date()
        val nowWeek = now.get(Calendar.WEEK_OF_YEAR)
        val nowYear = now.get(Calendar.YEAR)

        val menu = db.menuDao().getAll().firstOrNull()?.date ?: return true
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
                val day = key.toCalendar()
                day?.let {
                    menuList.add(DayMenu(day, foods.toArray().toList()))
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECT_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
            return INTERNAL_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        if (menuList.isEmpty()) {
            return UPDATE_ERROR
        }

        Collections.sort(menuList, ParserHelper.MenuDayComparator())
        db.menuDao().clear()
        val inserts = db.menuDao().insert(*menuList.toTypedArray())

        return if (inserts.all { it == -1L }) ALREADY_UPDATED else UPDATE_SUCCESS
    }

    /*
     * Serving stuff
     */

    fun getServings(): LiveData<List<Serving>> {
        if (servingLive.value == null) {
            mainDispatch {
                servingLive.value = db.servingDao().getToday()
                servingStatus.value = BUSY
            }
        }
        return servingLive
    }

    fun updateServings() {
        mainDispatch {
            if (context.isOnline()) {
                servingStatus.value = ioDispatch { downloadServingsTask() }
                servingLive.value = db.servingDao().getToday()
            } else {
                servingStatus.value = NO_ONLINE
            }
        }
    }

    private suspend fun downloadServingsTask(): StatusCode {
        try {
            val result = URL(SERVINGS_URL).downloadByGet()
            val items = JSONObject(result).getJSONObject("servings")

            val data = mutableListOf<Serving>()

            val keys = items.keys()
            while (keys.hasNext()) {
                val key = keys.next() as String

                val date = key.toCalendar("UTC")
                val ration = items.getInt(key)

                date?.let {
                    data.add(Serving(date, ration))
                }
            }
            Collections.sort(data, ParserHelper.ServingsComparator())

            if (data.isEmpty()) {
                return UPDATE_SUCCESS
            }

            db.servingDao().clear()
            db.servingDao().insert(*data.toTypedArray())
            return UPDATE_SUCCESS
        } catch (e: IOException) {
            e.printStackTrace()
            return CONNECT_ERROR
        } catch (e: JSONException) {
            return INTERNAL_ERROR
        } catch (e: NumberFormatException) {
            return INTERNAL_ERROR
        }
    }

    private fun mainDispatch(f: suspend (CoroutineScope) -> Unit) {
        viewModelScope.launch(Dispatchers.Main, block = f)
    }

    private suspend fun <T>ioDispatch(f: suspend (CoroutineScope) -> T): T {
        return withContext(coroutineContext + Dispatchers.IO, block = f)
    }

    companion object {
        private const val USER_URL = "http://uncmorfi.georgealegre.com/users?codes="
        private const val MENU_URL = "http://uncmorfi.georgealegre.com/menu"
        private const val SERVINGS_URL = "http://uncmorfi.georgealegre.com/servings"
    }

}