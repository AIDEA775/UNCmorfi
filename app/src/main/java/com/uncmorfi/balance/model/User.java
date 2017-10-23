package com.uncmorfi.balance.model;

import android.content.ContentValues;
import android.database.Cursor;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String card;
    private String name;
    private String type;
    private String image;
    private int balance;
    private long expiration;
    private long lastUpdate;
    private int position;

    public User() {}

    public User(Cursor cursor) {
        this.id = cursor.getInt(cursor.getColumnIndex(UsersContract.UserEntry._ID));
        this.card = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.CARD));
        this.name = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.NAME));
        this.type = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.TYPE));
        this.image = cursor.getString(cursor.getColumnIndex(UsersContract.UserEntry.IMAGE));
        this.balance = cursor.getInt(cursor.getColumnIndex(UsersContract.UserEntry.BALANCE));
        this.expiration = cursor.getLong(cursor.getColumnIndex(UsersContract.UserEntry.EXPIRATION));
        this.lastUpdate = cursor.getLong(cursor.getColumnIndex(UsersContract.UserEntry.LAST_UPDATE));
    }

    public int getId() {
        return id;
    }

    public int getBalance() {
        return balance;
    }

    public String getCard() {
        return card;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public long getExpiration() {
        return expiration;
    }

    public long getLastUpdate() {
        return lastUpdate;
    }

    public int getPosition() {
        return position;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCard(String card) {
        this.card = card;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setExpiration(long expiration) {
        this.expiration = expiration;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public ContentValues toContentValues (boolean complete) {
        ContentValues values = new ContentValues();
        values.put(UsersContract.UserEntry.CARD, this.card);
        values.put(UsersContract.UserEntry.BALANCE, this.balance);
        values.put(UsersContract.UserEntry.EXPIRATION, this.expiration);
        values.put(UsersContract.UserEntry.LAST_UPDATE, this.lastUpdate);
        if (complete) {
            values.put(UsersContract.UserEntry.NAME, this.name);
            values.put(UsersContract.UserEntry.TYPE, this.type);
            values.put(UsersContract.UserEntry.IMAGE, this.image);
        }
        return values;
    }
}
