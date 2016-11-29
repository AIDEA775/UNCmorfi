package com.uncmorfi.balance.userModel;

import android.provider.BaseColumns;

class UsersContract {
    static abstract class UserEntry implements BaseColumns {
        static final String TABLE_NAME ="user";

        static final String CARD = "card";
        static final String NAME = "name";
        static final String TYPE = "type";
        static final String IMAGE = "image";
        static final String BALANCE = "balance";
    }
}
