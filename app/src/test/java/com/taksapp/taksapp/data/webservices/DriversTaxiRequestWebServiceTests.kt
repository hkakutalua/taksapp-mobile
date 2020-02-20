package com.taksapp.taksapp.data.webservices

import com.nhaarman.mockitokotlin2.mock
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullAuthenticator
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestAcceptanceError.*
import com.taksapp.taksapp.utils.FileUtilities
import kotlinx.coroutines.runBlocking
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import okhttp3.mockwebserver.RecordedRequest
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration


@ExperimentalTime
class DriversTaxiRequestWebServiceTests {
    private val successfulTaxiRequestBodyPath =
        "json/drivers/me/taxi-requests/successful_taxi_request.json"
    private val errorTaxiRequestNotFoundBodyPath =
        "json/drivers/me/taxi-requests/error_taxi_request_not_found.json"
    private val errorTaxiRequestAlreadyAcceptedByYouBodyPath =
        "json/drivers/me/taxi-requests/error_taxi_request_already_accepted_by_you.json"
    private val errorTaxiRequestAlreadyAcceptedByAnotherDriverBodyPath =
        "json/drivers/me/taxi-requests/error_taxi_request_already_accepted_by_another_driver.json"
    private val errorTaxiRequestExpiredBodyPath =
        "json/drivers/me/taxi-requests/error_taxi_request_expired.json"
    private val errorTaxiRequestNotInAcceptedStatusBodyPath =
        "json/drivers/me/taxi-requests/error_taxi_request_not_in_accepted_status.json"

    @Test
    fun getsCurrentTaxiRequest() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTaxiRequestBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

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
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.getCurrentTaxiRequest()

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(TaxiRequestRetrievalError.NOT_FOUND, result.error)
        }
    }

    @Test
    fun getsTaxiRequestById() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTaxiRequestBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .getTaxiRequestById("eda69fe5-ac0d-4a95-9261-c54d17143bd4")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)
        }
    }

    @Test
    fun taxiRequestByIdDoesNotExists() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .getTaxiRequestById("eda69fe5-ac0d-4a95-9261-c54d17143bd4")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(TaxiRequestRetrievalError.NOT_FOUND, result.error)
        }
    }

    @Test
    fun acceptsTaxiRequest() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .acceptTaxiRequest("random-taxi-request-id")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.isSuccessful)
        }
    }

    @Test
    fun taxiRequestAcceptanceFailsWhenItsAlreadyAcceptedByYou() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(409)
                    .setBody(FileUtilities.getFileContent(errorTaxiRequestAlreadyAcceptedByYouBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .acceptTaxiRequest("random-taxi-request-id")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU, result.error)
        }
    }

    @Test
    fun taxiRequestAcceptanceFailsWhenItsAlreadyAcceptedByAnotherDriver() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(409)
                    .setBody(FileUtilities
                        .getFileContent(errorTaxiRequestAlreadyAcceptedByAnotherDriverBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .acceptTaxiRequest("random-taxi-request-id")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER, result.error)
        }
    }

    @Test
    fun taxiRequestAcceptanceFailsWhenItsExpired() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(409)
                    .setBody(FileUtilities
                        .getFileContent(errorTaxiRequestExpiredBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .acceptTaxiRequest("random-taxi-request-id")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TAXI_REQUEST_EXPIRED, result.error)
        }
    }

    @Test
    fun taxiRequestAcceptanceFailsWhenTaxiRequestIsNotFound() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(409)
                    .setBody(FileUtilities
                        .getFileContent(errorTaxiRequestNotFoundBodyPath))
            )
            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService
                .acceptTaxiRequest("random-taxi-request-id")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertEquals(TAXI_REQUEST_NOT_FOUND, result.error)
        }
    }

    @Test
    fun announcesArrival() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))

            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.announceArrival()

            // Assert
            Assert.assertTrue(result.isSuccessful)

            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertEquals(
                "/api/v1/drivers/me/taxi-requests/current/announce-arrival",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun arrivalAnnouncementFailsWhenTaxiRequestIsNotFound() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse()
                .setResponseCode(404)
                .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath)))

            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.announceArrival()

            // Assert
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(result.error, TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_FOUND)

            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun arrivalAnnouncementFailsWhenTaxiRequestIsNotInAcceptedStatus() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse()
                .setResponseCode(403)
                .setBody(FileUtilities.getFileContent(errorTaxiRequestNotInAcceptedStatusBodyPath)))

            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.announceArrival()

            // Assert
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(
                result.error,
                TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS)

            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun cancelCurrentTaxiRequest() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse().setResponseCode(204))

            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertTrue(result.isSuccessful)

            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertEquals(
                "/api/v1/drivers/me/taxi-requests/current/cancel",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun currentTaxiRequestCancellationFailsWhenTaxiIsNotFound() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(MockResponse()
                .setResponseCode(404)
                .setBody(FileUtilities.getFileContent(errorTaxiRequestNotFoundBodyPath)))

            val taxiRequestWebService =
                DriversTaxiRequestWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = taxiRequestWebService.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(
                TaxiRequestCancellationError.TAXI_REQUEST_NOT_FOUND,
                result.error
            )
        }
    }

    private fun RecordedRequest.getRequestMethod() =
        this.method?.toLowerCase()

    private fun RecordedRequest.getRequestPath() =
        this.requestUrl?.toUrl()?.path

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