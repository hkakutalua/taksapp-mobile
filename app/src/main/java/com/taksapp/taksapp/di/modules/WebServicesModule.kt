package com.taksapp.taksapp.di.modules

import com.taksapp.taksapp.BuildConfig
import com.taksapp.taksapp.data.infrastructure.services.SessionExpiryHandler
import com.taksapp.taksapp.data.infrastructure.services.SharedPreferencesSessionStore
import com.taksapp.taksapp.data.webservices.DevicesWebService
import com.taksapp.taksapp.data.webservices.RidersTaxiRequestWebService
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.SessionExpiryListener
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.AccessTokenInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.TokenRefreshAuthenticator
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import org.koin.dsl.module
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@UseExperimental(ExperimentalTime::class)
val webServicesModule = module {
    factory<RidersTaxiRequestService> { RidersTaxiRequestWebService(get()) }
    factory<DevicesService> { DevicesWebService(get()) }

    single {
        Taksapp.Builder()
            .environment(Environment.DEVELOPMENT)
            .client(
                OkHttpClientAdapter(
                    BuildConfig.BASE_URL,
                    30.toDuration(TimeUnit.SECONDS),
                    get<TokenRefreshAuthenticator>(),
                    get<AccessTokenInterceptor>()
                )
            )
            .jsonConverter(MoshiJsonConverterAdapter())
            .sessionStore(get())
            .sessionExpiredCallback(get())
            .build()
    }

    factory<SessionStore> { SharedPreferencesSessionStore(get()) }
    factory<SessionExpiryListener> { SessionExpiryHandler(get()) }
    factory { TokenRefreshAuthenticator(BuildConfig.BASE_URL, get(), get(), MoshiJsonConverterAdapter()) }
    factory { AccessTokenInterceptor(get()) }
}