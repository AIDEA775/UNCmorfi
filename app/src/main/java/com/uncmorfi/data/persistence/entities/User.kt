package com.uncmorfi.data.persistence.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.uncmorfi.shared.toMoneyFormat
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

    val balance: BigDecimal? = null,

    val price: BigDecimal? = null,

    val rations: Int? = null,

    val expiration: LocalDate = LocalDate.now(),

    val lastUpdate: Instant = Instant.now(),

    ) : Serializable {
    @Ignore
    var isLoading: Boolean = false

    fun anyRations() = rations ?: calculateRations() ?: 0

    fun calculateRations() =  balance
        ?.divide(price, 0, RoundingMode.DOWN)
        ?.toInt()

    fun balanceOrRations(): String = rations?.toString()
        ?: balance?.toMoneyFormat()
        ?: BigDecimal.ZERO.toMoneyFormat() // Podría explotar pero mejor no
}