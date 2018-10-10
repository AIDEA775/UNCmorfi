package com.uncmorfi.balance.model

import android.content.ContentValues
import android.database.Cursor

import java.io.Serializable

class User : Serializable {
    var id: Int = 0
    var card: String? = null
    var name: String? = null
    var type: String? = null
    var image: String? = null
    var balance: Int = 0
    var expiration: Long = 0
    var lastUpdate: Long = 0
    var position: Int? = null

    constructor()

    constructor(card : String) {
        this.card = card
    }

    constructor(cursor: Cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(UsersContract.UserEntry.ID))
        this.card = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.CARD))
        this.name = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.NAME))
        this.type = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.TYPE))
        this.image = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.IMAGE))
        this.balance = cursor.getInt(cursor.getColumnIndex(UsersContract.UserEntry.BALANCE))
        this.expiration = cursor.getLong(cursor.getColumnIndex(UsersContract.UserEntry.EXPIRATION))
        this.lastUpdate = cursor.getLong(cursor.getColumnIndex(UsersContract.UserEntry.LAST_UPDATE))
    }

    fun toContentValues(complete: Boolean): ContentValues {
        val values = ContentValues()
        values.put(UsersContract.UserEntry.CARD, this.card)
        values.put(UsersContract.UserEntry.BALANCE, this.balance)
        values.put(UsersContract.UserEntry.EXPIRATION, this.expiration)
        values.put(UsersContract.UserEntry.LAST_UPDATE, this.lastUpdate)
        values.put(UsersContract.UserEntry.IMAGE, this.image)
        values.put(UsersContract.UserEntry.TYPE, this.type)
        if (complete) {
            values.put(UsersContract.UserEntry.NAME, this.name)
        }
        return values
    }
}
