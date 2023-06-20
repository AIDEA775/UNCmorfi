package com.uncmorfi.data.repository

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.uncmorfi.R
import com.uncmorfi.shared.CHANNEL_ID
import com.uncmorfi.shared.DEFAULT_ID

class RepoNotify(val context: Context) {

    fun send(block: NotificationCompat.Builder.() -> Unit) {
        createChannel()
        update(build(block))
    }

    fun build(block: NotificationCompat.Builder.() -> Unit): Notification {
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setSmallIcon(R.drawable.ic_icon_mono)
            .setColor(context.getColor(R.color.accent))
            .setAutoCancel(true)

        block(builder)

        return builder.build()
    }

    fun update(notification: Notification, id: Int = DEFAULT_ID) {
        NotificationManagerCompat
            .from(context)
            .notify(id, notification)
    }

    fun remove(id: Int) {
        NotificationManagerCompat
            .from(context)
            .cancel(id)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = context.getString(R.string.channel_name)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance)
            channel.description = context.getString(R.string.channel_description)

            context.getSystemService(NotificationManager::class.java)
                ?.createNotificationChannel(channel)
        }
    }

}