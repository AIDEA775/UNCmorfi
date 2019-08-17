package com.uncmorfi.models

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "users")
data class User(
    @PrimaryKey() var card: String = "",
    var name: String? = null,
    var type: String? = null,
    var image: String? = null,
    var balance: Int = 0,
    var expiration: Long = 0,
    var lastUpdate: Long = 0,
    @Ignore var isLoading: Boolean = false
): Serializable