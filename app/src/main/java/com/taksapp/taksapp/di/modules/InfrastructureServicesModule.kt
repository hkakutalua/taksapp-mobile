package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.infrastructure.services.FirebasePushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.services.TimerTaskScheduler
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import org.koin.dsl.module
import kotlin.time.ExperimentalTime

@ExperimentalTime
val infrastructureServicesModule = module {
    factory<PushNotificationTokenRetriever> { FirebasePushNotificationTokenRetriever() }
    factory<TaskScheduler> { TimerTaskScheduler() }
}