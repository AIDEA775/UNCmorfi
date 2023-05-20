package com.uncmorfi.data.network

import com.uncmorfi.data.persistence.entities.Serving
import com.uncmorfi.shared.DateUtils.FORMAT_ARG4
import com.uncmorfi.shared.HUEMUL_URL
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object ServingParser {

    fun fetch(): List<Serving> {
        val response = okHttpClient.newCall(
            Request.Builder()
                .url(
                    HUEMUL_URL
                        .toHttpUrl()
                        .newBuilder()
                        .addQueryParameter("accion", "1")
                        .addQueryParameter("sede", "0475")
                        .build()
                )
                .build()
        ).execute()

        val body = response.body?.string()!!

//        println("body::: $body")

        return parseBody(body)
    }

    fun parseBody(body: String): List<Serving> {
        val jsObject = body
            .substringAfter("(")
            .substringBefore(")")

        val rows = jsObject
            .substringAfter("], rows: [")
            .substringBeforeLast("]")

        val strip = rows
            .replace("c:", "")
            .replace("v:", "")

        val tokens = strip
            .split("[\\s'{}\\[\\],]+".toRegex())
            .filter { it.isNotBlank() }
//        val tokens = "[\\d:]".toRegex().findAll(strip).map { it.value }.toList()
//        val tokens = rows.split("['},]*\\{v: '?".toRegex()).toTypedArray()

        return tokens.chunked(2) {
            val count = it[1].toInt()
            val time = LocalTime.parse(it[0], FORMAT_ARG4)
            val instant = time.atDate(LocalDate.now())
                .atZone(ZoneId.systemDefault())
                .toInstant()
            Serving(instant, count)
        }
    }

}