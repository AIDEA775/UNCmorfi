package com.uncmorfi.balance.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uncmorfi.balance.model.UsersContract.UserEntry;


class UsersDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "Users.db";

    UsersDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE " + UserEntry.TABLE_NAME + " ("
                + UserEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + UserEntry.CARD + " TEXT NOT NULL,"
                + UserEntry.NAME + " TEXT NOT NULL,"
                + UserEntry.TYPE + " TEXT NOT NULL,"
                + UserEntry.IMAGE + " TEXT NOT NULL,"
                + UserEntry.BALANCE + " INTEGER,"
                + UserEntry.EXPIRATION + "INTEGER,"
                + UserEntry.LAST_UPDATE + "INTEGER,"
                + "UNIQUE (" + UserEntry.CARD + "))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
        if (oldVersion < 2) {
            sqLiteDatabase.execSQL("ALTER TABLE " + UserEntry.TABLE_NAME + " ADD COLUMN "
                    + UserEntry.EXPIRATION + " INTEGER");
            sqLiteDatabase.execSQL("ALTER TABLE " + UserEntry.TABLE_NAME + " ADD COLUMN "
                    + UserEntry.LAST_UPDATE + " INTEGER");
        }
    }
}
