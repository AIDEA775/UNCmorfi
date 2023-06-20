package com.uncmorfi.data.worker

import android.content.Context
import android.content.Intent
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uncmorfi.MainActivity
import com.uncmorfi.R
import com.uncmorfi.data.repository.RepoNotify
import com.uncmorfi.shared.RESERVATION_PREF
import com.uncmorfi.shared.toPendingIntent
import java.time.LocalDate

class ReservationWorker(context: Context, p: WorkerParameters) : CoroutineWorker(context, p) {

    private val repoNotify = RepoNotify(context)
    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)

    override suspend fun doWork(): Result {

        val enabledDays = preferences.getStringSet(RESERVATION_PREF, emptySet())!!

        val today = LocalDate.now().dayOfWeek.value.toString()

        if (enabledDays.contains(today)) {
            repoNotify.send {
                setContentTitle(applicationContext.getString(R.string.notify_reserve_title))
                setContentText(applicationContext.getString(R.string.notify_reserve_text))
                setContentIntent(
                    Intent(applicationContext, MainActivity::class.java)
                        .toPendingIntent(applicationContext)
                )
            }
        }

        return Result.success()
    }

}
