package com.uncmorfi.data.network.models

data class ReservationResponse(
        var reservationResult: String = "",
        var token: String? = null,
        var path: String? = null
)