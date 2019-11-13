package com.uncmorfi.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uncmorfi.models.*
import com.uncmorfi.shared.*
import com.uncmorfi.shared.ReserveStatus.*
import com.uncmorfi.shared.StatusCode.*
import kotlinx.coroutines.*
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.*
import kotlin.coroutines.coroutineContext
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import com.google.gson.GsonBuilder

class MainViewModel(val context: Application): AndroidViewModel(context) {
    private val db: AppDatabase = AppDatabase(context)
    private val userLive: MutableLiveData<List<User>> = MutableLiveData()
    private val menuLive: MutableLiveData<List<DayMenu>> = MutableLiveData()
    private val servingLive: MutableLiveData<List<Serving>> = MutableLiveData()

    val status: MutableLiveData<StatusCode> = MutableLiveData()
    val reserveStatus: MutableLiveData<ReserveStatus> = MutableLiveData()
    val reserveTry: MutableLiveData<Int> = MutableLiveData()
    var reserveJob: Job? = null

    private val client by lazy {
        val okHttpClient = OkHttpClient.Builder()
                .readTimeout(1, TimeUnit.MINUTES)
                .connectTimeout(1, TimeUnit.MINUTES)
                .build()

        val gson = GsonBuilder()
                .registerTypeAdapter(Calendar::class.java, CalendarDeserializer())
                .create()

        Retrofit.Builder()
                .baseUrl("https://frozen-sierra-45328.herokuapp.com/")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(okHttpClient)
                .build().create(Webservice::class.java)
    }

    init {
        this.status.value = BUSY
        reserveStatus.value = NOCACHED
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
                val status = ioDispatch {
                    val cards = users.joinToString(separator = ",") { it.card }
                    val userUpdated = client.getUsers(cards)

                    val rows = db.userDao().upsertUser(*userUpdated.toTypedArray())
                    if (rows > 0) UPDATE_SUCCESS else USER_INSERTED
                }
                usersNotify(status!!)
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
            usersNotify(USER_DELETED)
        }
    }

    private suspend fun usersNotify(status: StatusCode) {
        userLive.value = db.userDao().getAll()
        this.status.value = status
    }

    /*
     * Menu stuff
     */
    fun getMenu(): LiveData<List<DayMenu>> {
        if (menuLive.value == null) {
            mainDispatch {
                menuLive.value = db.menuDao().getAll()
                this.status.value = BUSY
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
                this.status.value = ioDispatch {
                    val menu = client.getMenu()
                    val menuList = menu.menu.map { entry -> DayMenu(entry.key, entry.value) }

                    if (menuList.isEmpty()) {
                        return@ioDispatch UPDATE_ERROR
                    }
                    db.menuDao().clearOld()
                    val inserts = db.menuDao().insert(*menuList.toTypedArray())

                    if (inserts.all { it == -1L }) ALREADY_UPDATED else UPDATE_SUCCESS
                }

                menuLive.value = db.menuDao().getAll()
            } else {
                this.status.value = NO_ONLINE
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

        val menu = db.menuDao().getLast()?.date ?: return true
        val menuWeek = menu.get(Calendar.WEEK_OF_YEAR)
        val menuYear = now.get(Calendar.YEAR)

        return menuYear < nowYear || menuWeek < nowWeek
    }

    /*
     * Serving stuff
     */
    fun getServings(): LiveData<List<Serving>> {
        if (servingLive.value == null) {
            mainDispatch {
                servingLive.value = db.servingDao().getToday()
                this.status.value = BUSY
            }
        }
        return servingLive
    }

    fun updateServings() {
        mainDispatch {
            if (context.isOnline()) {
                this.status.value = ioDispatch {
                    val servings = client.getServings().servings.map {
                        entry -> Serving(entry.key, entry.value)
                    }

                    if (servings.isEmpty()) {
                        return@ioDispatch ALREADY_UPDATED
                    }
                    db.servingDao().clearOld()
                    val inserts = db.servingDao().insert(*servings.toTypedArray())

                    if (inserts.all { it == -1L }) ALREADY_UPDATED else UPDATE_SUCCESS
                }
                servingLive.value = db.servingDao().getToday()
            } else {
                this.status.value = NO_ONLINE
            }
        }
    }


    /*
     * Reservation stuff
     */
    fun reserveIsCached(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve != null) {
                reserveStatus.value = CACHED
            } else {
                reserveStatus.value = NOCACHED
            }
        }
    }

    fun reserveLogin(user: User, captcha: String) {
        mainDispatch {
            val reserve = client.getLogin(user.card)
            reserve.captchaText = captcha

            val result = ioDispatch { client.doLogin(reserve) }
            result?.let {
                insertReservation(result)
                reserveStatus.value = CACHED
            }
        }
    }

    fun reserveConsult(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve != null) {
                if (context.isOnline()) {
                    reserveStatus.value = CONSULTING
                    val result = ioDispatch { client.status(reserve) }
                    result?.updateReservation(reserve)
                } else {
                    this.status.value = NO_ONLINE
                }
            } else {
                reserveStatus.value = REDOLOGIN
            }
        }
    }

    fun reserve(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve != null) {
                if (context.isOnline()) {
                    reserveStatus.value = RESERVING
                    val result = ioDispatch { client.reserve(reserve) }
                    result?.updateReservation(reserve)
                } else {
                    this.status.value = NO_ONLINE
                }
            } else {
                reserveStatus.value = REDOLOGIN
            }
        }
    }

    fun reserveLoop(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve == null) {
                reserveStatus.value = REDOLOGIN
                return@mainDispatch
            }
            reserveJob?.cancel()
            reserveJob = mainDispatch{
                var intent = 0

                do {
                    intent += 1
                    reserveTry.value = intent
                    val result = ioDispatch { client.reserve(reserve) }
                    val status = result?.updateReservation(reserve)
                    delay(1500)
                } while (status != RESERVED && status != REDOLOGIN)

                reserveTry.value = 0
            }
        }
    }

    fun reserveStop() {
        reserveJob?.cancel()
        reserveJob = null
        reserveTry.value = 0
        status.value = BUSY
    }

    fun reserveLogout(user: User) {
        mainDispatch {
            db.reserveDao().delete(user.card)
            reserveStatus.value = NOCACHED
        }
    }

    private suspend fun ReservationResponse.updateReservation(reserve: Reservation): ReserveStatus {
        this.path?.let { reserve.path = it }
        this.token?.let { reserve.token = it }

        val status = ReserveStatus.valueOf(this.reservationResult.toUpperCase())
        reserveStatus.value = status

        if (status == REDOLOGIN) {
            db.reserveDao().delete(reserve.code)
        } else {
            insertReservation(reserve)
        }
        return status
    }

    private suspend fun insertReservation(reserve: Reservation) {
        // Guardar cookie con el codigo de la tarjeta a la que pertenece
        reserve.cookies?.map { c -> c.code = reserve.code }
        db.reserveDao().insert(reserve)
    }

    private suspend fun getReserve(code: String): Reservation? {
        val reserve = db.reserveDao().getReservation(code)
        reserve?.cookies = db.reserveDao().getCookies(code)
        return reserve
    }

    private fun mainDispatch(f: suspend (CoroutineScope) -> Unit): Job {
        return viewModelScope.launch(Dispatchers.Main, block = f)
    }

    private suspend fun <T>ioDispatch(f: suspend (CoroutineScope) -> T): T? {
        try {
            return withContext(coroutineContext + Dispatchers.IO, block = f)
        } catch (e: HttpException) {
            e.printStackTrace()
            this.status.value = CONNECT_ERROR
        } catch (e: IOException) {
            e.printStackTrace()
            this.status.value = CONNECT_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
            this.status.value =  INTERNAL_ERROR
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            this.status.value =  INTERNAL_ERROR
        }
        return null
    }

}