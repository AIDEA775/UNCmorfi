package com.uncmorfi.data.service

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.*
import com.uncmorfi.data.worker.CheckBalanceWorker
import com.uncmorfi.data.worker.ReservationWorker
import com.uncmorfi.shared.*
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class ServWorkers @Inject constructor(
    @ApplicationContext val context: Context
) {

    private val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    private val workManager = WorkManager.getInstance(context)

    fun refreshAllWorkers() {
        refreshLowBalanceWorker()
        refreshReservationWorker()
    }

    private fun refreshLowBalanceWorker() {
        val enabled = preferences.getBoolean(LOW_BALANCE_PREF, true)

        if (!enabled) {
            workManager.cancelUniqueWork(LOW_BALANCE_WORK)
            return
        }

        workManager.enqueueUniquePeriodicWork(
            LOW_BALANCE_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            PeriodicWorkRequestBuilder<CheckBalanceWorker>(1, TimeUnit.DAYS)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInitialDelay(1, TimeUnit.DAYS)
                .build()
        )
    }

    private fun refreshReservationWorker() {
        val days = preferences.getStringSet(RESERVATION_PREF, emptySet())!!

        if (days.isEmpty()) {
            workManager.cancelUniqueWork(RESERVATION_WORK)
            return
        }

        val delay = DateUtils.delayToNext(RESERVATIONS_OPEN)
        Log.i("ServWorkers", "refreshReservationWorker: InitialDelay: $delay")

        workManager.enqueueUniquePeriodicWork(
            RESERVATION_WORK,
            ExistingPeriodicWorkPolicy.REPLACE,
            PeriodicWorkRequestBuilder<ReservationWorker>(
                24, TimeUnit.HOURS,
                5, TimeUnit.MINUTES
            )
                .setInitialDelay(delay.toMinutes(), TimeUnit.MINUTES)
                .build()
        )
    }

}