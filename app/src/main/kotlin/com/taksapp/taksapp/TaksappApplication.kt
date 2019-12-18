package com.taksapp.taksapp

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaksappApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@TaksappApplication)
            androidLogger()
            modules(mutableListOf(
                com.taksapp.taksapp.di.modules.infrastructureServicesModule,
                com.taksapp.taksapp.di.modules.repositoriesModule,
                com.taksapp.taksapp.di.modules.webServicesModule,
                com.taksapp.taksapp.di.modules.viewModelsModule)
            )
        }
    }
}