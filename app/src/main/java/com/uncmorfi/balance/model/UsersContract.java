package com.uncmorfi.balance.model;

import android.provider.BaseColumns;

public class UsersContract {
    public static abstract class UserEntry implements BaseColumns {
        static final String TABLE_NAME = "user";

        public static final String CARD = "card";
        public static final String NAME = "name";
        public static final String TYPE = "type";
        public static final String IMAGE = "image";
        public static final String BALANCE = "balance";
        public static final String EXPIRATION = "expiration";
        public static final String LAST_UPDATE = "last_update";
    }
}
