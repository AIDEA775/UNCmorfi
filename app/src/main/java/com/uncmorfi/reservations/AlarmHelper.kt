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
import java.util.*

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

        private fun getIntent(context: Context, dayOfWeek: Int): PendingIntent {
            return Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(
                        context,
                        dayOfWeek,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT)
            }
        }

        fun scheduleAlarm(context: Context, dayOfWeek: Int, schedule: Boolean) {
            val intent = getIntent(context, dayOfWeek)
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            if (!schedule) {
                alarmManager.cancel(intent)
                return
            }

            val calendar = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_WEEK, dayOfWeek)
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
            }

            // Si ya pasó ese día, va para la otra semana
            if (calendar.timeInMillis < System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 7)
            }

            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    AlarmManager.INTERVAL_DAY * 7,
                    intent)
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
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true)

            with(NotificationManagerCompat.from(context)) {
                // notificationId es un int único para cada notificación
                val notificationId = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                notify(notificationId, builder.build())
            }
        }

        private const val CHANNEL_ID = "reservations"
    }
}