package com.uncmorfi.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.util.*

@Entity(tableName = "users")
data class User(
        @PrimaryKey() var card: String = "",
        var name: String? = null,
        var type: String? = null,
        var image: String? = null,
        var balance: Int = 0,
        var expiration: Calendar = Calendar.getInstance(),
        var lastUpdate: Calendar = Calendar.getInstance(),
        @Ignore var isLoading: Boolean = false
): Serializable