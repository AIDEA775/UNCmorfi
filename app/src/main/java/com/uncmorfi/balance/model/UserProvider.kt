package com.uncmorfi.balance.model

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri

import com.uncmorfi.balance.model.UsersContract.UserEntry

class UserProvider : ContentProvider() {

    private var mUsersDbHelper: UsersDbHelper? = null

    override fun onCreate(): Boolean {
        mUsersDbHelper = UsersDbHelper(context)
        return true
    }

    override fun query(uri: Uri, projection: Array<String>?, selection: String?,
                       selectionArgs: Array<String>?, sortOrder: String?): Cursor? {
        val db = mUsersDbHelper!!.readableDatabase

        val c: Cursor
        when (mUriMatcher.match(uri)) {
            USERS -> c = db.query(
                    UserEntry.TABLE_NAME,
                    projection,
                    selection,
                    selectionArgs, null, null,
                    sortOrder)
            USER_ID -> c = db.query(
                    UserEntry.TABLE_NAME,
                    projection,
                    UserEntry.ID + "=" + uri.lastPathSegment,
                    selectionArgs, null, null,
                    sortOrder)
            else -> throw IllegalArgumentException("URI not supported: $uri")
        }
        return c
    }

    override fun getType(uri: Uri): String? {
        when (mUriMatcher.match(uri)) {
            USERS -> return "vnd.android.cursor.dir/vnd.$authority/users"
            USER_ID -> return "vnd.android.cursor.item/vnd.$authority/users"
            else -> return null
        }
    }

    override fun insert(uri: Uri, contentValues: ContentValues?): Uri? {
        val newUserId: Long

        val db = mUsersDbHelper!!.writableDatabase
        newUserId = db.insert(UserEntry.TABLE_NAME, null, contentValues)

        db.close()
        return ContentUris.withAppendedId(CONTENT_URI, newUserId)
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
        var where = selection

        if (mUriMatcher.match(uri) == USER_ID) {
            where = UserEntry.ID + "=" + uri.lastPathSegment
        }

        val db = mUsersDbHelper!!.writableDatabase
        val result = db.delete(UserEntry.TABLE_NAME, where, selectionArgs)

        db.close()
        return result
    }

    override fun update(uri: Uri, contentValues: ContentValues?, selection: String?,
                        selectionArgs: Array<String>?): Int {
        var where = selection

        if (mUriMatcher.match(uri) == USER_ID) {
            where = UserEntry.ID + "=" + uri.lastPathSegment
        }

        val db = mUsersDbHelper!!.writableDatabase
        val result = db.update(UserEntry.TABLE_NAME, contentValues, where, selectionArgs)

        db.close()
        return result
    }

    companion object {
        private const val authority = "com.uncmorfi"

        val CONTENT_URI: Uri = Uri.parse("content://$authority/users")

        private const val USERS = 1
        private const val USER_ID = 2
        private val mUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

        init {
            mUriMatcher.addURI(authority, "users", USERS)
            mUriMatcher.addURI(authority, "users/#", USER_ID)
        }
    }
}
