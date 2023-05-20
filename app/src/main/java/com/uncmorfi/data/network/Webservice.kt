package com.uncmorfi.data.network

import com.uncmorfi.data.network.models.MenuRes
import com.uncmorfi.data.network.models.ReservationRes
import com.uncmorfi.data.network.models.ServingsRes
import com.uncmorfi.data.persistence.entities.Reservation
import com.uncmorfi.data.persistence.entities.User
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface Webservice {
    @GET("/users")
    suspend fun getUsers(@Query("codes") codes: String): List<User>

    @GET("/menu")
    suspend fun getMenu(): MenuRes

    @GET("/servings")
    suspend fun getServings(): ServingsRes

    @GET("/reservation/login")
    suspend fun getLogin(@Query("code") code: String): Reservation

    @POST("/reservation/login")
    suspend fun doLogin(@Body reservation: Reservation): Reservation

    @POST("/reservation/reserve")
    suspend fun reserve(@Body reservation: Reservation): ReservationRes

    @POST("/reservation/status")
    suspend fun status(@Body reservation: Reservation): ReservationRes
}