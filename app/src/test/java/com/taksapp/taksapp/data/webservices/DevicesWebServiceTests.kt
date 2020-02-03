package com.taksapp.taksapp.data.webservices

import com.nhaarman.mockitokotlin2.mock
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullAuthenticator
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalCoroutinesApi
@ExperimentalTime
class DevicesWebServiceTests {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    @Test
    fun updatesUserDevice() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))
            val devicesWebService = DevicesWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = devicesWebService.registerUserDevice(
                "push-notification-token",
                DevicesService.Platform.ANDROID
            )

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
        }
    }

    private fun getTaksapp(baseUrl: String): Taksapp {
        return Taksapp.Builder()
            .environment(Environment.PRODUCTION)
            .client(
                OkHttpClientAdapter(
                    baseUrl,
                    timeout = 30.toDuration(TimeUnit.SECONDS),
                    tokenRefreshAuthenticator = NullAuthenticator(),
                    accessTokenInterceptor = NullInterceptor()
                )
            )
            .jsonConverter(MoshiJsonConverterAdapter())
            .sessionStore(mock())
            .sessionExpiredCallback(mock())
            .build()
    }
}