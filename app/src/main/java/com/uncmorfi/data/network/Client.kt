package com.uncmorfi.data.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

private val logger = object : HttpLoggingInterceptor.Logger {
    override fun log(message: String) {
        Log.v("http", message)
    }
}
private val loggingInterceptor = HttpLoggingInterceptor(logger).apply {
    level = HttpLoggingInterceptor.Level.BODY
}

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor(loggingInterceptor)
    .readTimeout(1, TimeUnit.MINUTES)
    .connectTimeout(1, TimeUnit.MINUTES)
    .build()