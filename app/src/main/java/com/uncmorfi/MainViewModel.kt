package com.uncmorfi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.GsonBuilder
import com.uncmorfi.data.network.Webservice
import com.uncmorfi.data.network.models.ReservationResponse
import com.uncmorfi.data.persistence.AppDatabase
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.data.persistence.entities.Reservation
import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.data.repository.RepoUser
import com.uncmorfi.data.repository.RepoMenu
import com.uncmorfi.shared.*
import com.uncmorfi.shared.ReserveStatus.*
import com.uncmorfi.shared.StatusCode.*
import kotlinx.coroutines.*
import okhttp3.OkHttpClient
import org.json.JSONException
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.time.LocalDate
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

class MainViewModel(val context: Application) : AndroidViewModel(context) {
    private val db: AppDatabase = AppDatabase(context)
    private val repoMenu = RepoMenu(context)
    private val repoUser = RepoUser(context)
    private val userLive: MutableLiveData<List<User>> = MutableLiveData()
    private val servingLive: MutableLiveData<List<Serving>> = MutableLiveData()

    val isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val status: MutableLiveData<StatusCode> = MutableLiveData()
    val reservation: MutableLiveData<ReserveStatus> = MutableLiveData()
    val reserveTry: MutableLiveData<Int> = MutableLiveData()
    var reserveJob: Job? = null

    private val okHttpClient = OkHttpClient.Builder()
        .readTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .build()

    private val gson = GsonBuilder()
        .registerTypeAdapter(Calendar::class.java, CalendarDeserializer())
        .create()

    private val clientBeta by lazy {
        Retrofit.Builder()
            .baseUrl("https://frozen-sierra-45328.herokuapp.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build().create(Webservice::class.java)
    }

    private val client by lazy {
        Retrofit.Builder()
            .baseUrl("https://uncmorfi.georgealegre.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build().create(Webservice::class.java)
    }

    init {
        status.value = BUSY
        reservation.value = NOCACHED
    }

    /*
     * Balance stuff
     */

    fun allUsers(): LiveData<List<User>> = repoUser.getAll()

    fun updateCards(card: String) = viewModelScope.launch(Dispatchers.IO) {
        if (!context.isOnline()) {
            status.postValue(NO_ONLINE)
            return@launch
        }
        val updates = repoUser.fetch(card)
        status.postValue(if (updates > 0) UPDATE_SUCCESS else USER_INSERTED)
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
                usersNotify(status)
            } else {
                usersNotify(NO_ONLINE)
            }
        }
    }

    fun updateUserName(user: User) = viewModelScope.launch {
        repoUser.fullUpdate(user)
        status.value = UPDATE_SUCCESS
    }

    fun deleteUser(user: User) {
        mainDispatch {
            db.userDao().delete(user)
            // fixme eliminar tambien la caché del codigo de barras de la tarjeta
            usersNotify(USER_DELETED)
        }
    }

    private suspend fun usersNotify(code: StatusCode?) {
        userLive.value = db.userDao().getAll()
        status.value = code
    }

    /*
     * Menu stuff
     */
    fun getMenu(): LiveData<List<DayMenu>> = repoMenu.getAll()

    fun refreshMenu() = viewModelScope.launch(Dispatchers.IO) {
        if (needAutoUpdateMenu()) {
            forceRefreshMenu()
        }
    }

    fun forceRefreshMenu() = viewModelScope.launch(Dispatchers.IO) {
        Log.d("ViewModel", "Force update menu")
        if (!context.isOnline()) {
            status.postValue(NO_ONLINE)
            return@launch
        }
        status.postValue(UPDATING)
        val inserts = repoMenu.update()

        status.postValue(when {
            inserts.isEmpty() -> UPDATE_ERROR
            inserts.all { it == -1L } -> ALREADY_UPDATED
            else -> UPDATE_SUCCESS
        })
    }

    fun clearMenu() = viewModelScope.launch(Dispatchers.IO) {
        repoMenu.clear()
        forceRefreshMenu()
    }

    private suspend fun needAutoUpdateMenu(): Boolean {
        val now = LocalDate.now()
        val last = repoMenu.last() ?: return true
        return last.date.isBefore(now)
    }

    /*
     * Serving stuff
     */
    fun getServings(): LiveData<List<Serving>> {
        if (servingLive.value == null) {
            mainDispatch {
                servingLive.value = db.servingDao().getToday()
                status.value = BUSY
            }
        }
        return servingLive
    }

    fun updateServings() {
        mainDispatch {
            if (context.isOnline()) {
                status.value = ioDispatch {
                    val servings = client.getServings().servings.map { entry ->
                        Serving(entry.key, entry.value)
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
                status.value = NO_ONLINE
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
                reservation.value = CACHED
            } else {
                reservation.value = NOCACHED
            }
        }
    }

    fun reserveLogin(user: User, captcha: String) {
        mainDispatch {
            val reserve = clientBeta.getLogin(user.card)
            reserve.captchaText = captcha

            val result = ioDispatch { clientBeta.doLogin(reserve) }
            result?.let {
                insertReservation(result)
                reservation.value = CACHED
            }
        }
    }

    fun reserveConsult(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve != null) {
                if (context.isOnline()) {
                    reservation.value = CONSULTING
                    val result = ioDispatch { clientBeta.status(reserve) }
                    result?.updateReservation(reserve)
                } else {
                    status.value = NO_ONLINE
                }
            } else {
                reservation.value = REDOLOGIN
            }
        }
    }

    fun reserve(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve != null) {
                if (context.isOnline()) {
                    reservation.value = RESERVING
                    val result = ioDispatch { clientBeta.reserve(reserve) }
                    result?.updateReservation(reserve)
                } else {
                    status.value = NO_ONLINE
                }
            } else {
                reservation.value = REDOLOGIN
            }
        }
    }

    fun reserveLoop(user: User) {
        mainDispatch {
            val reserve = getReserve(user.card)
            if (reserve == null) {
                reservation.value = REDOLOGIN
                return@mainDispatch
            }
            reserveJob?.cancel()
            reserveJob = mainDispatch {
                var intent = 0

                do {
                    intent += 1
                    reserveTry.value = intent
                    val result = ioDispatch { clientBeta.reserve(reserve) }
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
            reservation.value = NOCACHED
        }
    }

    private suspend fun ReservationResponse.updateReservation(reserve: Reservation): ReserveStatus {
        this.path?.let { reserve.path = it }
        this.token?.let { reserve.token = it }

        val status = ReserveStatus.valueOf(this.reservationResult.toUpperCase())
        reservation.value = status

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

    private suspend fun <T> ioDispatch(f: suspend (CoroutineScope) -> T): T? {
        var result: T? = null
        isLoading.value = true
        try {
            result = withContext(coroutineContext + Dispatchers.IO, block = f)
        } catch (e: HttpException) {
            e.printStackTrace()
            status.value = CONNECT_ERROR
        } catch (e: IOException) {
            e.printStackTrace()
            status.value = CONNECT_ERROR
        } catch (e: JSONException) {
            e.printStackTrace()
            status.value = INTERNAL_ERROR
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            status.value = INTERNAL_ERROR
        } finally {
            isLoading.value = false
        }
        return result
    }

}