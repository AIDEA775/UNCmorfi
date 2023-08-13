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
    @PrimaryKey val code: String = "",
    val token: String = "",
    val path: String = "",
){
    @Ignore val cookies: List<Cookie>? = null

    @Ignore val captchaText: String? = null
}