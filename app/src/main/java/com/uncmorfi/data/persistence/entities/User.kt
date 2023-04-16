package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.*

@Entity(tableName = "users")
data class User(
        @PrimaryKey @SerializedName("code") var card: String = "",
        var name: String? = null,
        var type: String? = null,
        @SerializedName("imageURL") var image: String? = null,
        var balance: Int = 0,
        @SerializedName("expirationDate")
        var expiration: Calendar = Calendar.getInstance(),
        var lastUpdate: Calendar = Calendar.getInstance(),

        @Ignore var isLoading: Boolean = false
): Serializable