package com.uncmorfi.models

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.*

interface Webservice {
    @GET("/users")
    suspend fun getUsers(@Query("codes") codes: String): List<User>

    @GET("/menu")
    suspend fun getMenu(): Menu

    @GET("/servings")
    suspend fun getServings(): Servings

    @GET("/reservation/login")
    suspend fun getLogin(@Query("code") code: String): Reservation

    @POST("/reservation/login")
    suspend fun doLogin(@Body reservation: Reservation): Reservation

    @POST("/reservation/reserve")
    suspend fun reserve(@Body reservation: Reservation): ReservationResponse

    @POST("/reservation/status")
    suspend fun status(@Body reservation: Reservation): ReservationResponse
}