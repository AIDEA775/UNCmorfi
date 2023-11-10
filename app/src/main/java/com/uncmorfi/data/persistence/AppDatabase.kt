package com.uncmorfi.data.persistence

import androidx.room.Database
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
    version = 4
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun menuDao(): DayMenuDao
    abstract fun servingDao(): ServingDao
    abstract fun reserveDao(): ReservationDao
}