package com.uncmorfi.models

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Webservice {
    @GET("/reservation/login")
    suspend fun login(@Query("code") code: String): Reservation

    @POST("/reservation/reserve")
    suspend fun reserve(@Body reservation: Reservation): ReservationResponse
}