package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.BuildConfig
import com.taksapp.taksapp.data.infrastructure.SessionExpiryHandler
import com.taksapp.taksapp.data.infrastructure.SharedPreferencesSessionStore
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.SessionExpiredCallback
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@UseExperimental(ExperimentalTime::class)
val webServicesModule = module {
    single {
        Taksapp.Builder()
            .environment(Environment.DEVELOPMENT)
            .client(OkHttpClientAdapter(
                    BuildConfig.BASE_URL,
                    timeout = 30.toDuration(TimeUnit.SECONDS)))
            .jsonConverter(MoshiJsonConverterAdapter())
            .sessionStore(get())
            .sessionExpiredCallback(get())
            .build()
    }

    factory<SessionStore> { SharedPreferencesSessionStore(get()) }
    factory<SessionExpiredCallback> { SessionExpiryHandler(get()) }
}