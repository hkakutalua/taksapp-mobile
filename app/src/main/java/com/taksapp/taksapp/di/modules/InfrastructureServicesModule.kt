package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.data.infrastructure.services.FirebasePushNotificationTokenRetriever
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import org.koin.dsl.module

val infrastructureServicesModule = module {
    factory<PushNotificationTokenRetriever> { FirebasePushNotificationTokenRetriever() }
}