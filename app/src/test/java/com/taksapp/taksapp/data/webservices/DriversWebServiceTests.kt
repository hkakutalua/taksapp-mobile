package com.taksapp.taksapp.data.webservices

import com.nhaarman.mockitokotlin2.mock
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullAuthenticator
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.interfaces.DriversService
import com.taksapp.taksapp.utils.FileUtilities
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class DriversWebServiceTests {
    private val successfulOnlineUpdateBodyPath =
        "json/drivers/me/status/successful_online_update.json"
    private val noDeviceRegisteredErrorBodyPath =
        "json/drivers/me/status/error_no_device_registered.json"

    @Test
    fun setsAsOnline() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulOnlineUpdateBodyPath))
            )

            val driversWebService = DriversWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = driversWebService.setAsOnline()

            // Assert
            val sentRequest = server.takeRequest(1, TimeUnit.MILLISECONDS)
            Assert.assertNotNull(sentRequest)
            Assert.assertTrue(sentRequest?.requestUrl?.toString()
                ?.contains("api/v1/drivers/me/status/online")!!)
            Assert.assertTrue(result.isSuccessful)
        }
    }

    @Test
    fun settingAsOnlineFailsDueToDriverNotHavingDevice() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(403)
                    .setBody(FileUtilities.getFileContent(noDeviceRegisteredErrorBodyPath))
            )

            val driversWebService = DriversWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = driversWebService.setAsOnline()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(DriversService.OnlineSwitchError.DRIVER_HAS_NO_DEVICE, result.error)
        }
    }

    @Test
    fun setsAsOffline() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))

            val driversWebService = DriversWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = driversWebService.setAsOffline()

            // Assert
            val sentRequest = server.takeRequest(1, TimeUnit.MILLISECONDS)
            Assert.assertNotNull(sentRequest)
            Assert.assertTrue(sentRequest?.requestUrl?.toString()
                ?.contains("api/v1/drivers/me/status/offline")!!)
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