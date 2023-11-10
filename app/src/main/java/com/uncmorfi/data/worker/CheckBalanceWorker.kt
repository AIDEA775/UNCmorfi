package com.uncmorfi.data.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uncmorfi.MainActivity
import com.uncmorfi.R
import com.uncmorfi.data.repository.RepoNotify
import com.uncmorfi.data.repository.RepoUser
import com.uncmorfi.shared.WARNING_USER_RATIONS
import com.uncmorfi.shared.toPendingIntent
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class CheckBalanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted p: WorkerParameters,
    private val repoUser: RepoUser,
    private val repoNotify: RepoNotify
) : CoroutineWorker(context, p) {

    override suspend fun doWork() = withContext(Dispatchers.IO) {

        // Refresh all
        for (u in repoUser.getAll()) {
            repoUser.fetch(u.card)
        }

        // Find low balances
        val toWarn = repoUser.getAll().filter { it.anyRations() <= WARNING_USER_RATIONS }

        if (toWarn.isNotEmpty()) {
            repoNotify.send {
                setContentTitle(applicationContext.getString(R.string.notify_recharge_title))
                setContentText(applicationContext.getString(R.string.notify_recharge_text))
                setContentIntent(
                    Intent(applicationContext, MainActivity::class.java)
                        .toPendingIntent(applicationContext)
                )
            }
        }

        return@withContext Result.success()
    }
}