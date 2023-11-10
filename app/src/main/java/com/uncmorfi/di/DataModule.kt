package com.uncmorfi.di

import android.content.Context
import androidx.room.Room
import com.uncmorfi.data.persistence.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    fun providesDataBase(@ApplicationContext context : Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "database.db"
    ).fallbackToDestructiveMigration()
        .build()

    @Provides
    fun providesUserDao(appDatabase: AppDatabase) = appDatabase.userDao()

    @Provides
    fun providesMenuDao(appDatabase: AppDatabase) = appDatabase.menuDao()

    @Provides
    fun providesServingDao(appDatabase: AppDatabase) = appDatabase.servingDao()

    @Provides
    fun providesReservationDao(appDatabase: AppDatabase) = appDatabase.reserveDao()
}