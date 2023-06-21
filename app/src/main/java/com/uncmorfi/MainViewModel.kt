package com.uncmorfi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.uncmorfi.data.persistence.entities.DayMenu
import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.data.repository.RepoMenu
import com.uncmorfi.data.repository.RepoServings
import com.uncmorfi.data.repository.RepoUser
import com.uncmorfi.data.service.ServWorkers
import com.uncmorfi.shared.*
import com.uncmorfi.shared.ReserveStatus.*
import com.uncmorfi.shared.StatusCode.*
import kotlinx.coroutines.*
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate
import java.util.*

class MainViewModel(val context: Application) : AndroidViewModel(context) {
    private val repoMenu = RepoMenu(context)
    private val repoUser = RepoUser(context)
    private val repoServings = RepoServings(context)
    private val servWorkers = ServWorkers(context)

    val status: MutableLiveData<StatusCode> = MutableLiveData()

    init {
        status.value = BUSY
    }

    /*
     * Balance stuff
     */

    fun getAllUsers(): LiveData<List<User>> = repoUser.listenAll()

    fun getUser(card: String): LiveData<User?> = repoUser.getBy(card)

    fun updateCards(card: String) = launchIO {
        if (!context.isOnline()) {
            status.postValue(NO_ONLINE)
            return@launchIO
        }
        status.postValue(UPDATING)
//        delay(4_000)
        val updates = repoUser.fetch(card)
        status.postValue(if (updates > 0) UPDATE_SUCCESS else USER_INSERTED)
    }

    fun updateUserName(user: User) = launchIO {
        repoUser.fullUpdate(user)
        status.postValue(UPDATE_SUCCESS)
    }

    fun deleteUser(user: User) = launchIO {
        repoUser.delete(user)
        status.postValue(USER_DELETED)
    }

    /*
     * Menu stuff
     */

    fun getMenu(): LiveData<List<DayMenu>> = repoMenu.getAll()

    fun refreshMenu() = launchIO {
        if (needAutoUpdateMenu()) {
            forceRefreshMenu()
        }
    }

    fun forceRefreshMenu() = launchIO {
        Log.d("ViewModel", "Force update menu")
        if (!context.isOnline()) {
            status.postValue(NO_ONLINE)
            return@launchIO
        }
        status.postValue(UPDATING)
        val inserts = repoMenu.update()
        Log.d("ViewModel", "menu update result: $inserts")

        status.postValue(when {
            inserts.isEmpty() -> UPDATE_ERROR
            inserts.all { it == -1L } -> ALREADY_UPDATED
            else -> UPDATE_SUCCESS
        })
    }

    fun clearMenu() = launchIO {
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

    fun getServings(): LiveData<List<Serving>> = repoServings.getToday()

    fun updateServings() = launchIO {
        Log.d("ViewModel", "Update serving")
        if (!context.isOnline()) {
            status.postValue(NO_ONLINE)
            return@launchIO
        }
        status.postValue(UPDATING)
        val inserts = repoServings.update()

        status.postValue(when {
            inserts.isEmpty() -> EMPTY_UPDATE
            inserts.all { it == -1L } -> ALREADY_UPDATED
            else -> UPDATE_SUCCESS
        })
    }

    /*
     * Worker stuff
     */

    fun refreshWorkers() {
        servWorkers.refreshAllWorkers()
    }

    private fun launchIO(f: suspend (CoroutineScope) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                f.invoke(this)
            } catch (e: HttpException) {
                e.printStackTrace()
                status.postValue(CONNECT_ERROR)
            } catch (e: IOException) {
                e.printStackTrace()
                status.postValue(CONNECT_ERROR)
            } catch (e: JSONException) {
                e.printStackTrace()
                status.postValue(INTERNAL_ERROR)
            } catch (e: Exception) {
                e.printStackTrace()
                status.postValue(INTERNAL_ERROR)
            }
        }
    }

}