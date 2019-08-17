package com.uncmorfi.models

import android.provider.BaseColumns

class UsersContract {
    abstract class UserEntry : BaseColumns {
        companion object {
            internal const val TABLE_NAME = "user"

            const val ID = BaseColumns._ID
            const val CARD = "card"
            const val NAME = "name"
            const val TYPE = "type"
            const val IMAGE = "image"
            const val BALANCE = "balance"
            const val EXPIRATION = "expiration"
            const val LAST_UPDATE = "last_update"
        }
    }
}
