package com.uncmorfi.data.network

import android.util.Log
import com.uncmorfi.data.persistence.entities.User
import com.uncmorfi.shared.DateUtils.FORMAT_JS
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.util.concurrent.TimeUnit

object BalanceParser {
    private val logger = object : HttpLoggingInterceptor.Logger {
        override fun log(message: String) {
            Log.v("http", message)
        }
    }
    private val loggingInterceptor = HttpLoggingInterceptor(logger).apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .readTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .build()

    fun fetch(card: String): User? {
        return try {

            val response = okHttpClient.newCall(
                Request.Builder()
                    .url("https://comedor.unc.edu.ar/gv-ds.php")
                    .post(
                        FormBody.Builder()
                            .add("accion", "4")
                            .add("codigo", card)
                            .build()
                    )
                    .build()
            ).execute()

            val body = response.body?.string()!!

            println("body::: $body")

            val left = body.indexOf("rows: [{c: [")
            val right = body.indexOf("]", left)
            val result = body.substring(left + 12, right - 2)
            val tokens = result.split("['},]*\\{v: '?".toRegex()).toTypedArray()
            if (tokens.size < 2) return null

            val user = User(
                card = card,
                name = tokens[17],
                type = tokens[9],
                email = tokens[22],
                image = "https://asiruws.unc.edu.ar/foto/" + tokens[25],
                balance = BigDecimal(tokens[6]),
                price = BigDecimal(tokens[23]).minus(BigDecimal(tokens[12])),
                expiration = parseExpiration(tokens[5]),
                lastUpdate = Instant.now(),
            )

            user
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun parseExpiration(token: String): LocalDate = LocalDate
        .parse(token, FORMAT_JS)
        .plusMonths(1) // JS indexa el mes a partir de 0
}