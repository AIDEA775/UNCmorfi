package com.uncmorfi.reservations

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import androidx.core.content.ContextCompat.getSystemService
import com.uncmorfi.MainActivity
import com.uncmorfi.R
import com.uncmorfi.shared.compareToTodayInMillis
import java.util.*
import java.util.Calendar.*

class AlarmHelper {
    companion object {

        fun createNotificationChannel(context: Context) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val name = context.getString(R.string.reservations_channel_name)
                val descriptionText = context.getString(R.string.reservations_channel_description)
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                }
                val notificationManager: NotificationManager =
                        getSystemService(context, NotificationManager::class.java)!!
                notificationManager.createNotificationChannel(channel)
            }
        }

        fun enableBootReceiver(context: Context) {
            val receiver = ComponentName(context, BootReceiver::class.java)
            context.packageManager.setComponentEnabledSetting(
                    receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP
            )
        }

        private fun getIntent(context: Context): PendingIntent {
            return Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                        context,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        fun getNextAlarm(context: Context): Calendar? {
            val sharedPref = context.getSharedPreferences("prefs", Context.MODE_PRIVATE)

            val today = Calendar.getInstance().get(DAY_OF_WEEK)
            val calendar = Calendar.getInstance().apply {
                set(MINUTE, 0)
                set(SECOND, 0)
                set(MILLISECOND, 0)
            }

            for (i in today..today+7) {
                val day = i%7
                val value = sharedPref.getInt(day.toString(), -1)

                if (setAsNextAlarm(calendar, value)) {
                    return calendar
                }
                calendar.add(DAY_OF_YEAR, 1)
            }
            return null
        }

        /* -1 si no está seteado
         * 1 si es a las 7AM
         * 2 si es a las 10AM
         * 3 si es a las 7AM o las 10AM
        */
        private fun setAsNextAlarm(calendar: Calendar, value: Int): Boolean {
            if (value == 1 || value == 3) {
                calendar.set(HOUR_OF_DAY, 6)
                calendar.set(MINUTE, 55)
                if (calendar.compareToTodayInMillis() > 0) {
                    return true
                }
            }

            if (value == 2 || value == 3) {
                calendar.set(HOUR_OF_DAY, 9)
                calendar.set(MINUTE, 55)
                if (calendar.compareToTodayInMillis() > 0) {
                    return true
                }
            }
            return false
        }

        fun scheduleAlarm(context: Context, calendar: Calendar) {
            val intent = getIntent(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (Build.VERSION.SDK_INT >= 19) {
                alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        intent)
            } else {
                alarmManager.set(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        intent)
            }
        }

        fun cancelAlarm(context: Context) {
            val intent = getIntent(context)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(intent)
        }

        fun makeNotification(context: Context) {
            val resultIntent = Intent(context, MainActivity::class.java)
            val resultPendingIntent = TaskStackBuilder.create(context).run {
                addNextIntentWithParentStack(resultIntent)
                getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
            }

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setContentIntent(resultPendingIntent)
                    .setSmallIcon(R.drawable.nav_ticket)
                    .setContentTitle(context.getString(R.string.reservations_title))
                    .setContentText(context.getString(R.string.reservations_content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId es un int único para cada notificación
                val notificationId = Calendar.getInstance().get(DAY_OF_YEAR)
                notify(notificationId, builder.build())
            }
        }

        private const val CHANNEL_ID = "reservations"
    }
}