package com.uncmorfi.data.network

import android.util.Log
import com.google.gson.GsonBuilder
import com.uncmorfi.shared.CalendarDeserializer
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.*
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

private val gson = GsonBuilder()
    .registerTypeAdapter(Calendar::class.java, CalendarDeserializer())
    .create()

val clientBeta by lazy {
    Retrofit.Builder()
        .baseUrl("https://frozen-sierra-45328.herokuapp.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build().create(Webservice::class.java)
}

val clientRest by lazy {
    Retrofit.Builder()
        .baseUrl("https://uncmorfi.georgealegre.com/")
        .addConverterFactory(GsonConverterFactory.create(gson))
        .client(okHttpClient)
        .build().create(Webservice::class.java)
}