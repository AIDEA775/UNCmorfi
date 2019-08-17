package com.uncmorfi.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

import com.uncmorfi.models.UsersContract.UserEntry

internal class UsersDbHelper(context: Context) :
        SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(sqLiteDatabase: SQLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + UserEntry.TABLE_NAME + " ("
                + UserEntry.ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UserEntry.CARD + " TEXT NOT NULL,"
                + UserEntry.NAME + " TEXT NOT NULL,"
                + UserEntry.TYPE + " TEXT NOT NULL,"
                + UserEntry.IMAGE + " TEXT NOT NULL,"
                + UserEntry.BALANCE + " INTEGER,"
                + UserEntry.EXPIRATION + " INTEGER,"
                + UserEntry.LAST_UPDATE + " INTEGER,"
                + "UNIQUE (" + UserEntry.CARD + "))")
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + UserEntry.TABLE_NAME + " ADD COLUMN "
                    + UserEntry.EXPIRATION + " INTEGER")
            sqLiteDatabase.execSQL("ALTER TABLE " + UserEntry.TABLE_NAME + " ADD COLUMN "
                    + UserEntry.LAST_UPDATE + " INTEGER")
        }
    }

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "Users.db"
    }
}
