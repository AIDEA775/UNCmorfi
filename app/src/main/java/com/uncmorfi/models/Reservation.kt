package com.uncmorfi.models

import androidx.room.*
import androidx.room.ForeignKey.CASCADE

@Entity(tableName = "reservations",
        foreignKeys = [ForeignKey(
                entity = User::class,
                onDelete = CASCADE,
                parentColumns = arrayOf("card"),
                childColumns = arrayOf("code"))])
data class Reservation(
        @PrimaryKey var code: String = "",
        var token: String = "",
        @Ignore var cookies: List<Cookie>? = null,
        var path: String = "",
        @Ignore var captchaText: String? = null
)

@Entity(tableName = "cookies",
        foreignKeys = [ForeignKey(
                entity = Reservation::class,
                onDelete = CASCADE,
                parentColumns = arrayOf("code"),
                childColumns = arrayOf("code_id"))])
data class Cookie(
        @PrimaryKey(autoGenerate = true) var cookieId: Int = 0,
        @ColumnInfo(name = "code_id") var code: String,

        var domain: String = "",
        var value: String = "",
        var name: String = ""
)

data class ReservationResponse(
        var reservationResult: String = "",
        var token: String? = null,
        var path: String? = null
)