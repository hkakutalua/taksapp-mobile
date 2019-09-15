package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.BuildConfig
import com.taksapp.taksapp.data.infrastructure.SessionExpiryHandler
import com.taksapp.taksapp.data.infrastructure.SharedPreferencesTokensStore
import com.taksapp.taksapp.data.webservices.client.AuthenticationTokensStore
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.SessionExpiredCallback
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.DurationUnit
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
            .authenticationTokensStore(get())
            .sessionExpiredCallback(get())
            .build()
    }

    factory<AuthenticationTokensStore> { SharedPreferencesTokensStore(get()) }
    factory<SessionExpiredCallback> { SessionExpiryHandler(get()) }
}