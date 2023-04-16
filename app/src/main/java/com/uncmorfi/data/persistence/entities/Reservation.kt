package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity(tableName = "reservations",
        foreignKeys = [ForeignKey(
            entity = User::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = arrayOf("card"),
            childColumns = arrayOf("code")
        )])
data class Reservation(
    @PrimaryKey var code: String = "",
    var token: String = "",
    @Ignore var cookies: List<Cookie>? = null,
    var path: String = "",
    @Ignore var captchaText: String? = null
)