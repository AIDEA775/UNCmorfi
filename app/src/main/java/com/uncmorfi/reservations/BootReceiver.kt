package com.uncmorfi.reservations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val calendar = AlarmHelper.getNextAlarm(context)
            calendar?.let {
                AlarmHelper.scheduleAlarm(context, calendar)
            }
        }
    }
}
