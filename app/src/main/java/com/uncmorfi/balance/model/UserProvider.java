package com.uncmorfi.balance.model;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.uncmorfi.balance.model.UsersContract.UserEntry;



public class UserProvider extends ContentProvider {
    private static final String authority = "com.uncmorfi";

    public static final Uri CONTENT_URI = Uri.parse("content://" + authority + "/users");

    private UsersDbHelper mUsersDbHelper;

    private static final int USERS = 1;
    private static final int USER_ID = 2;
    private static final UriMatcher mUriMatcher;

    static {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mUriMatcher.addURI(authority, "users", USERS);
        mUriMatcher.addURI(authority, "users/#", USER_ID);
    }

    @Override
    public boolean onCreate() {
        mUsersDbHelper = new UsersDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mUsersDbHelper.getReadableDatabase();

        Cursor c;

        switch (mUriMatcher.match(uri)) {
            case USERS:
                c = db.query(UserEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            default:
                throw new IllegalArgumentException("URI not supported: " + uri);
        }
        return c;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case USERS:
                return "vnd.android.cursor.dir/vnd." + authority + "/users";
            case USER_ID:
                return "vnd.android.cursor.item/vnd." + authority + "/users";
            default:
                return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(@NonNull Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
