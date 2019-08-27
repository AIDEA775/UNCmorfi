package com.uncmorfi.reservations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import java.util.*

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == "android.intent.action.BOOT_COMPLETED") {
            val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

            for (day in Calendar.MONDAY..Calendar.FRIDAY) {
                val isSchedule = sharedPref.getBoolean(day.toString(), false)
                AlarmHelper.scheduleAlarm(context, day, isSchedule)
            }
        }
    }
}
