package com.taksapp.taksapp.data.webservices

import com.nhaarman.mockitokotlin2.mock
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullAuthenticator
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.domain.interfaces.TaxiRequestRetrievalError
import com.taksapp.taksapp.utils.FileUtilities
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
class RidersTaxiRequestWebServiceTests {
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private val successfulTaxiRequestBodyPath =
        "json/riders/me/taxi-requests/successful_taxi_request.json"
    private val errorTaxiRequestActiveBodyPath =
        "json/riders/me/taxi-requests/error_taxi_request_active.json"
    private val errorNoAvailableDriversBodyPath =
        "json/riders/me/taxi-requests/error_no_available_drivers.json"
    private val errorNoRegisteredDeviceBodyPath =
        "json/riders/me/taxi-requests/error_no_registered_device.json"
    private val errorTaxiRequestNotFoundBodyPath =
        "json/riders/me/taxi-requests/error_taxi_request_not_found.json"

    @Test
    fun sendsTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(FileUtilities.getFileContent(successfulTaxiRequestBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .sendTaxiRequest(Location(0.0, 0.0), Location(0.293, 0.45))

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
            Assert.assertEquals(Status.WAITING_ACCEPTANCE, result.data?.status)
        }
    }

    @Test
    fun taxiRequestFailsDueToExistingActiveTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(409)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestActiveBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .sendTaxiRequest(Location(0.0, 0.0), Location(0.293, 0.45))

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS, result.error)
        }
    }

    @Test
    fun taxiRequestFailsDueToNoAvailableDrivers() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(403)
                    .setBody(FileUtilities.getFileContent(errorNoAvailableDriversBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .sendTaxiRequest(Location(0.0, 0.0), Location(0.293, 0.45))

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TaxiRequestError.NO_AVAILABLE_DRIVERS, result.error)
        }
    }

    @Test
    fun taxiRequestFailsDueToRiderNotHavingDevice() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(403)
                    .setBody(FileUtilities.getFileContent(errorNoRegisteredDeviceBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .sendTaxiRequest(Location(0.0, 0.0), Location(0.293, 0.45))

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TaxiRequestError.DEVICE_NOT_REGISTERED, result.error)
        }
    }

    @Test
    fun cancelsCurrentTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
        }
    }

    @Test
    fun cancellationFailsWhenCurrentTaxiRequestDoesNotExists() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(CancellationError.TAXI_REQUEST_NOT_FOUND, result.error)
        }
    }

    @Test
    fun getsCurrentTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTaxiRequestBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.getCurrentTaxiRequest()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)
        }
    }

    @Test
    fun currentTaxiRequestDoesNotExists() {
        coroutineScope.launch {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath))
            )
            val taxiRequestWebService =
                RidersTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.getCurrentTaxiRequest()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(TaxiRequestRetrievalError.NOT_FOUND, result.error)
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