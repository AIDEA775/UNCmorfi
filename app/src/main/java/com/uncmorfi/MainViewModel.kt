package com.uncmorfi

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.data.repository.RepoMenu
import com.uncmorfi.data.repository.RepoServings
import com.uncmorfi.data.repository.RepoUser
import com.uncmorfi.data.service.ServWorkers
import com.uncmorfi.shared.*
import com.uncmorfi.shared.StatusCode.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import org.json.JSONException
import retrofit2.HttpException
import java.io.IOException
import java.time.LocalDate

class MainViewModel(val context: Application) : AndroidViewModel(context) {
    private val repoMenu = RepoMenu(context)
    private val repoUser = RepoUser(context)
    private val repoServings = RepoServings(context)
    private val servWorkers = ServWorkers(context)

    private val _state = MutableStateFlow(BUSY)
    val state = _state.asStateFlow()

    val users = repoUser.listenAll()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val servings = repoServings.getToday()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    val menu = repoMenu.getAll()
        .distinctUntilChanged()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            _state.update { BUSY }
            updateServings()
        }
    }

    /*
     * Balance stuff
     */
    fun getUser(card: String): Flow<User?> = repoUser.getBy(card)

    fun updateCards(card: String) = launchIO {
        if (!context.isOnline()) {
            _state.update { NO_ONLINE }
            return@launchIO
        }
        _state.update { UPDATING }
//        delay(4_000)
        val updates = repoUser.fetch(card)
        _state.update {
            if (updates > 0) UPDATE_SUCCESS else USER_INSERTED
        }
    }

    fun updateUserName(user: User) = launchIO {
        repoUser.fullUpdate(user)
        _state.update { UPDATE_SUCCESS }
    }

    fun deleteUser(user: User) = launchIO {
        repoUser.delete(user)
        _state.update { USER_DELETED }
    }

    /*
     * Menu stuff
     */
    fun refreshMenu() = launchIO {
        if (needAutoUpdateMenu()) {
            forceRefreshMenu()
        }
    }

    fun forceRefreshMenu() = launchIO {
        Log.d("ViewModel", "Force update menu")
        if (!context.isOnline()) {
            _state.update { NO_ONLINE }
            return@launchIO
        }
        _state.update { UPDATING }
        val inserts = repoMenu.update()
        Log.d("ViewModel", "menu update result: $inserts")

        _state.update {
            when {
                inserts.isEmpty() -> UPDATE_ERROR
                inserts.all { it == -1L } -> ALREADY_UPDATED
                else -> UPDATE_SUCCESS
            }
        }
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

    fun updateServings() = launchIO {
        Log.d("ViewModel", "Update serving")
        if (!context.isOnline()) {
            _state.update { NO_ONLINE }
            return@launchIO
        }
        _state.update { UPDATING }
        val inserts = repoServings.update()
        _state.update {
            when {
                inserts.isEmpty() -> EMPTY_UPDATE
                inserts.all { it == -1L } -> ALREADY_UPDATED
                else -> UPDATE_SUCCESS
            }
        }
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
                _state.update { CONNECT_ERROR }
            } catch (e: IOException) {
                e.printStackTrace()
                _state.update { CONNECT_ERROR }
            } catch (e: JSONException) {
                e.printStackTrace()
                _state.update { INTERNAL_ERROR }
            } catch (e: Exception) {
                e.printStackTrace()
                _state.update { INTERNAL_ERROR }
            }
        }
    }
}