package com.uncmorfi.balance.userModel;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.uncmorfi.balance.userModel.UsersContract.UserEntry;


public class UsersDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "Users.db";

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

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + UserEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public void saveNewUser(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        sqLiteDatabase.insert(UserEntry.TABLE_NAME, null, user.toContentValues());

        sqLiteDatabase.close();
    }

    public void updateUserBalance(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues newUserBalance = new ContentValues();
        newUserBalance.put(UserEntry.BALANCE, user.getBalance());

        sqLiteDatabase.update(
                UserEntry.TABLE_NAME,
                newUserBalance,
                UserEntry.CARD + " = ?",
                new String[]{user.getCard()}
        );
        sqLiteDatabase.close();
    }

    public void updateUserName(User user) {
        SQLiteDatabase sqLiteDatabase = getWritableDatabase();

        ContentValues newUserName = new ContentValues();
        newUserName.put(UserEntry.NAME, user.getName());

        sqLiteDatabase.update(
                UserEntry.TABLE_NAME,
                newUserName,
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
}
