package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.cache.TaxiRequestCache
import com.taksapp.taksapp.data.infrastructure.services.FirebasePushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.services.TimerTaskScheduler
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val infrastructureServicesModule = module {
    factory<PushNotificationTokenRetriever> { FirebasePushNotificationTokenRetriever() }
    factory { TaxiRequestCache(androidContext()) }
    factory<TaskScheduler> { TimerTaskScheduler() }
}