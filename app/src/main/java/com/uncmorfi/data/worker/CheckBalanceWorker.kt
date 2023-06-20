package com.uncmorfi.data.worker

import android.content.Context
import android.content.Intent
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.uncmorfi.MainActivity
import com.uncmorfi.R
import com.uncmorfi.data.repository.RepoNotify
import com.uncmorfi.data.repository.RepoUser
import com.uncmorfi.shared.WARNING_USER_RATIONS
import com.uncmorfi.shared.toPendingIntent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class CheckBalanceWorker(context: Context, p: WorkerParameters) : CoroutineWorker(context, p) {

    private val repoUser = RepoUser(context)
    private val repoNotify = RepoNotify(context)

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
