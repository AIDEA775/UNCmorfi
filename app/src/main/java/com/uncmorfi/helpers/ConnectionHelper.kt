package com.uncmorfi.helpers

import android.content.Context
import android.net.ConnectivityManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

fun Context?.isOnline(): Boolean {
    return (this?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
            .activeNetworkInfo?.isConnected ?: false
}

fun URL.downloadByGet(): String {
    val conn = this.openConnection() as HttpURLConnection
    conn.readTimeout = 10000
    conn.connectTimeout = 15000
    conn.requestMethod = "GET"
    conn.doInput = true
    conn.connect()
    val inputStream = conn.inputStream

    val rd = BufferedReader(InputStreamReader(inputStream))
    var line: String
    val response = StringBuilder()
    while (true) {
        line = rd.readLine() ?: break
        response.append(line)
    }
    rd.close()

    return response.toString()
}