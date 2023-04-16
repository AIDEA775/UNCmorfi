package com.uncmorfi.data.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uncmorfi.data.persistence.dao.DayMenuDao
import com.uncmorfi.data.persistence.dao.ReservationDao
import com.uncmorfi.data.persistence.dao.ServingDao
import com.uncmorfi.data.persistence.dao.UserDao
import com.uncmorfi.data.persistence.entities.*

@Database(
    entities = [
        User::class,
        DayMenu::class,
        Serving::class,
        Reservation::class,
        Cookie::class,
    ],
    version = 1
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun menuDao(): DayMenuDao
    abstract fun servingDao(): ServingDao
    abstract fun reserveDao(): ReservationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        operator fun invoke(context: Context): AppDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "database.db"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}