package com.uncmorfi.userSQL;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uncmorfi.userSQL.UsersContract.UserEntry;

public class UsersDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "Users.db";

    public UsersDbHelper(Context context) {
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
                + "UNIQUE (" + UserEntry.CARD + "))");
    }

    public void saveNewUser(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.insert(UserEntry.TABLE_NAME, null, user.toContentValues());

        sqLiteDatabase.close();
    }

    public void updateUserBalance(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put(UserEntry.BALANCE, user.getBalance());

        sqLiteDatabase.update(
                UserEntry.TABLE_NAME,
                content,
                UserEntry.CARD + " = ?",
                new String[]{user.getCard()}
        );
        sqLiteDatabase.close();
    }

    public void updateUserName(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues content = new ContentValues();
        content.put(UserEntry.NAME, user.getName());

        sqLiteDatabase.update(
                UserEntry.TABLE_NAME,
                content,
                UserEntry.CARD + " = ?",
                new String[]{user.getCard()}
        );
        sqLiteDatabase.close();
    }

    public void deleteUserByCard(String card) {
        if (card != null) {
            SQLiteDatabase sqLiteDatabase = getWritableDatabase();
            sqLiteDatabase.delete(UserEntry.TABLE_NAME, UserEntry.CARD + " = ?", new String[]{card});
            sqLiteDatabase.close();
        }
    }

    public Cursor getUserByCard(String card) {
        return getReadableDatabase().query(
                UserEntry.TABLE_NAME,
                null,
                UserEntry.CARD + " = ?",
                new String[]{card},
                null,
                null,
                null);
    }

    public Cursor getAllUsers() {
        return getReadableDatabase().query(
                UserEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // todo pensar en esto
    }
}
