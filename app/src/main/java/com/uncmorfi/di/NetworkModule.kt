package com.uncmorfi.di

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.uncmorfi.BuildConfig
import com.uncmorfi.data.network.Webservice
import com.uncmorfi.shared.CalendarDeserializer
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Url
    fun provideUrl() : String = "https://frozen-sierra-45328.herokuapp.com/"
    //https://uncmorfi.georgealegre.com/

    private val logger = HttpLoggingInterceptor.Logger { message -> Log.v("http", message) }

    private val loggingInterceptor = HttpLoggingInterceptor(logger).apply {
        if (BuildConfig.DEBUG) level = HttpLoggingInterceptor.Level.BODY
    }

    @Provides
    fun providesOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .readTimeout(1, TimeUnit.MINUTES)
        .connectTimeout(1, TimeUnit.MINUTES)
        .build()

    @Provides
    fun providesGson() : Gson = GsonBuilder()
        .registerTypeAdapter(Calendar::class.java, CalendarDeserializer())
        .create()

    @Provides
    fun providesRetrofit(@Url url : String,gson: Gson, okHttpClient: OkHttpClient) = Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(okHttpClient)
            .build()

    @Provides
    fun providesWebService(retrofit: Retrofit) = retrofit.create(Webservice::class.java)
}

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class Url