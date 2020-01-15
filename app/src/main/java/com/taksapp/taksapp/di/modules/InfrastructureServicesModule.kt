package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.infrastructure.FirebasePushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.PushNotificationTokenRetriever
import org.koin.dsl.module

val infrastructureServicesModule = module {
    factory<PushNotificationTokenRetriever> { FirebasePushNotificationTokenRetriever() }
}