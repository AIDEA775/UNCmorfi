package com.uncmorfi.shared.connectivity

import kotlinx.coroutines.flow.Flow

interface ConnectivityObserver {

    fun observe(): Flow<Status>

    enum class Status {
        Available, Unavailable, Losing, Lost
    }
}

fun ConnectivityObserver.Status?.isOnline() = this == ConnectivityObserver.Status.Available