package com.uncmorfi.reservations

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            AlarmHelper.makeNotification(context)

            val calendar = AlarmHelper.getNextAlarm(context)
            calendar?.let {
                AlarmHelper.scheduleAlarm(context, calendar)
            }
        }
    }
}