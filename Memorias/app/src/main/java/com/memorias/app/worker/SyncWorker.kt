package com.memorias.app.worker

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.memorias.app.domain.usecase.SyncFavoritesUseCase
import com.memorias.app.domain.usecase.SyncSessionsUseCase
import java.time.Duration
import androidx.work.Constraints

class SyncWorker(
    context: Context,
    workerParams: WorkerParameters,
    private val syncFavorites: SyncFavoritesUseCase,
    private val syncSessions: SyncSessionsUseCase,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        var anyFailed = false

        syncFavorites().onError { anyFailed = true }
        syncSessions().onError { anyFailed = true }

        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        const val WORK_NAME = "memorias_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(Duration.ofHours(1))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build(),
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    Duration.ofMinutes(5),
                )
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                request,
            )
        }
    }
}