package com.taksapp.taksapp

import androidx.multidex.MultiDexApplication
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaksappApplication : MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TaksappApplication)
            androidLogger()
            modules(mutableListOf(
                com.taksapp.taksapp.di.modules.infrastructureServicesModule,
                com.taksapp.taksapp.di.modules.repositoriesModule,
                com.taksapp.taksapp.di.modules.webServicesModule,
                com.taksapp.taksapp.di.modules.viewModelsModule,
                com.taksapp.taksapp.di.modules.backgroundHandlersModule
            ))
        }
    }

    override fun onTerminate() {
        super.onTerminate()
        stopKoin()
    }
}