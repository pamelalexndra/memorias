package com.memorias.app.core.factory

import android.content.Context
import com.memorias.app.core.di.AppContainer
import com.memorias.app.worker.SyncWorker
import com.memorias.worker.WidgetRefreshWorker

class MemoriasWorkerFactory(
    private val container: AppContainer,
) : WorkerFactory() {

    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters,
    ): ListenableWorker? = when (workerClassName) {

        SyncWorker::class.java.name ->
            SyncWorker(
                context       = appContext,
                workerParams  = workerParameters,
                syncFavorites = container.syncFavoritesUseCase(),
                syncSessions  = container.syncSessionsUseCase(),
            )

        WidgetRefreshWorker::class.java.name ->
            WidgetRefreshWorker(
                context      = appContext,
                workerParams = workerParameters,
            )

        else -> null
    }
}