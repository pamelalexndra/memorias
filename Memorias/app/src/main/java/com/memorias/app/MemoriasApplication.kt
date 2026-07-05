package com.memorias.app

import android.app.Application
import androidx.work.Configuration
import com.memorias.app.core.di.AppContainer
import com.memorias.app.core.factory.MemoriasWorkerFactory
import com.memorias.app.worker.SyncWorker
import com.memorias.worker.WidgetRefreshWorker

class MemoriasApplication : Application(), Configuration.Provider {

    lateinit var container: AppContainer
        private set

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(MemoriasWorkerFactory(container))
            .build()

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        scheduleBackgroundWork()
    }

    private fun scheduleBackgroundWork() {
        SyncWorker.schedule(this)
        WidgetRefreshWorker.schedule(this)
    }
}