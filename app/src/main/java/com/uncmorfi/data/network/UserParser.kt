package com.uncmorfi.data.network

import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.DateUtils.FORMAT_JS
import com.uncmorfi.shared.HUEMUL_URL
import com.uncmorfi.shared.PROFILE_PIC_URL
import com.uncmorfi.shared.toBigDecimalOrZero
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject

class UserParser @Inject constructor(
    private val okHttpClient: OkHttpClient
) {

    fun fetch(card: String): User? {
        return try {
            val response = okHttpClient.newCall(
                Request.Builder()
                    .url(HUEMUL_URL)
                    .post(
                        FormBody.Builder()
                            .add("accion", "4")
                            .add("codigo", card)
                            .build()
                    )
                    .build()
            ).execute()

            val body = response.body?.string()!!

//            println("body::: $body")

            parse(body)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parse(body: String): User? {
        val left = body.indexOf("rows: [{c: [")
        val right = body.indexOf("]", left)
        val result = body.substring(left + 12, right - 2)
        val tokens = result.split("['},]*\\{v: '?".toRegex()).toTypedArray()
        if (tokens.size < 2) return null

        return User(
            card = tokens[26],
            name = tokens[17],
            type = tokens[9],
            email = tokens[21],
            image = PROFILE_PIC_URL + tokens[25],
            balance = tokens[6].toBigDecimalOrNull(),
            price = tokens[23].toBigDecimalOrNull()?.minus(tokens[12].toBigDecimalOrZero()),
            rations = tokens[7].toIntOrNull(),
            expiration = parseExpiration(tokens[5]),
            lastUpdate = Instant.now(),
        )
    }

    fun parseExpiration(token: String): LocalDate = LocalDate
        .parse(token, FORMAT_JS)
        .plusMonths(1) // JS indexa el mes a partir de 0
}