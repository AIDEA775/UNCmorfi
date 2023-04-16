package com.uncmorfi.data.persistence.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "cookies",
        foreignKeys = [ForeignKey(
            entity = Reservation::class,
            onDelete = ForeignKey.CASCADE,
            parentColumns = arrayOf("code"),
            childColumns = arrayOf("code_id")
        )])
data class Cookie(
    @PrimaryKey(autoGenerate = true) var cookieId: Int = 0,
    @ColumnInfo(name = "code_id", index = true) var code: String,

    var domain: String = "",
    var value: String = "",
    var name: String = ""
)