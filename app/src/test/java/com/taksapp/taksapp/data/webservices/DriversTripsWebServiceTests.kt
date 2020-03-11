@file:Suppress("BlockingMethodInNonBlockingContext")

package com.taksapp.taksapp.data.webservices

import com.nhaarman.mockitokotlin2.mock
import com.taksapp.taksapp.data.webservices.client.Environment
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullAuthenticator
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.NullInterceptor
import com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient.OkHttpClientAdapter
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiJsonConverterAdapter
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.interfaces.DriversTripsService.*
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
class DriversTripsWebServiceTests {
    private val successfulTripBodyPath =
        "json/drivers/me/trips/successful_trip.json"
    private val errorTripNotFoundBodyPath =
        "json/drivers/me/trips/error_trip_not_found.json"
    private val errorActiveTripNotFoundBodyPath =
        "json/drivers/me/trips/error_active_trip_not_found.json"
    private val errorNoActiveTaxiRequestToStartTripFromBodyPath =
        "json/drivers/me/trips/error_no_active_taxi_request_to_start_trip_from.json"

    @Test
    fun startsTrip() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(201)
                    .setBody(FileUtilities.getFileContent(successfulTripBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.startTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)

            Assert.assertEquals(
                "/api/v1/drivers/me/trips",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "post",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun failsToStartTripWhenThereIsNoActiveTaxiRequest() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorNoActiveTaxiRequestToStartTripFromBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.startTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.hasFailed)

            Assert.assertEquals(TripStartError.NO_TAXI_REQUEST_TO_START_TRIP_FROM, result.error)
        }
    }

    @Test
    fun getsCurrentTrip() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTripBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.getCurrentTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)

            Assert.assertEquals(
                "/api/v1/drivers/me/trips/current",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "get",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun currentTripDoesNotExists() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorActiveTripNotFoundBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.getCurrentTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(TripRetrievalError.TRIP_NOT_FOUND, result.error)
        }
    }

    @Test
    fun getsTripById() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTripBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService
                .getTripById("eda69fe5-ac0d-4a95-9261-c54d17143bd4")

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)

            Assert.assertEquals(
                "/api/v1/drivers/me/trips/eda69fe5-ac0d-4a95-9261-c54d17143bd4",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "get",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun tripByIdDoesNotExists() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorTripNotFoundBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService
                .getTripById("eda69fe5-ac0d-4a95-9261-c54d17143bd4")

            // Assert
            Assert.assertNotNull(server.takeRequest(1, TimeUnit.MILLISECONDS))
            Assert.assertTrue(result.hasFailed)
            Assert.assertEquals(TripRetrievalError.TRIP_NOT_FOUND, result.error)
        }
    }

    @Test
    fun addLocationsToCurrentTripRoute() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTripBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.addLocationToCurrentTripRoute(listOf(
                    Location(1.0293, 0.2934),
                    Location(1.0293, 0.2934),
                    Location(1.0293, 0.2934)
                ))

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)

            Assert.assertEquals(
                "/api/v1/drivers/me/trips/current/route",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun failsToAddLocationsToCurrentTripRouteWhenThereIsNoActiveTrip() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorActiveTripNotFoundBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.addLocationToCurrentTripRoute(listOf(
                Location(1.0293, 0.2934),
                Location(1.0293, 0.2934),
                Location(1.0293, 0.2934)
            ))

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.hasFailed)

            Assert.assertEquals(LocationToRouteError.TRIP_NOT_FOUND, result.error)
        }
    }

    @Test
    fun finishesCurrentTrip() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(200)
                    .setBody(FileUtilities.getFileContent(successfulTripBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.finishCurrentTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.isSuccessful)
            Assert.assertNotNull(result.data)

            Assert.assertEquals(
                "/api/v1/drivers/me/trips/current/finish",
                request?.getRequestPath()
            )

            Assert.assertEquals(
                "patch",
                request?.getRequestMethod()
            )
        }
    }

    @Test
    fun failsToFinishCurrentTripWhenThereIsNoActiveTrip() {
        runBlocking {
            // Arrange
            val server = MockWebServer()
            server.enqueue(
                MockResponse()
                    .setResponseCode(404)
                    .setBody(FileUtilities.getFileContent(errorActiveTripNotFoundBodyPath))
            )
            val tripsWebService =
                DriversTripsWebService(getTaksapp(server.url("").toString()))

            // Act
            val result = tripsWebService.finishCurrentTrip()

            // Assert
            val request = server.takeRequest(1, TimeUnit.MILLISECONDS)

            Assert.assertNotNull(request)
            Assert.assertTrue(result.hasFailed)

            Assert.assertEquals(TripFinishError.TRIP_NOT_FOUND, result.error)
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