package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.io.Serializable
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "users")
data class User(
    @PrimaryKey
    val card: String = "",

    val name: String = "",

    val type: String = "",

    val email: String = "",

    val image: String = "",

    val balance: BigDecimal = BigDecimal.ZERO,

    val price: BigDecimal = BigDecimal.ZERO,

    val expiration: LocalDate = LocalDate.now(),

    val lastUpdate: Instant = Instant.now(),

    ) : Serializable {
    @Ignore
    var isLoading: Boolean = false

    fun rations() = balance
        .divide(price, 0, RoundingMode.DOWN)
        .toInt()
}