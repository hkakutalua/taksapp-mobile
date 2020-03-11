package com.taksapp.taksapp.data.webservices.client.resources.drivers

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.resources.common.ErrorResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.common.LocationRequestBody
import com.taksapp.taksapp.data.webservices.client.resources.common.TaxiRequestResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.common.TripResponseBody

class DriversResource(configurationProvider: ConfigurationProvider) {
    val me = CurrentDriverResource(configurationProvider)

    class CurrentDriverResource(private val configurationProvider: ConfigurationProvider) {
        enum class OnlineSwitchApiError { NO_DEVICE_REGISTERED, SERVER_ERROR }
        enum class TaxiRetrievalApiError { TAXI_REQUEST_NOT_FOUND, SERVER_ERROR }
        enum class TaxiRequestAcceptanceApiError {
            ALREADY_ACCEPTED_BY_YOU,
            ALREADY_ACCEPTED_BY_ANOTHER_DRIVER,
            EXPIRED,
            NOT_FOUND,
            SERVER_ERROR
        }

        enum class TaxiRequestAnnounceApiError {
            TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS,
            NOT_FOUND,
            SERVER_ERROR
        }

        enum class TaxiRequestCancellationApiError {
            NOT_FOUND,
            SERVER_ERROR
        }

        enum class TripStartApiError {
            NO_TAXI_REQUEST_TO_START_TRIP_FROM,
            SERVER_ERROR
        }

        enum class TripRetrievalApiError {
            NO_TRIP_FOUND,
            SERVER_ERROR
        }

        enum class LocationToRouteApiError {
            ACTIVE_TRIP_NOT_FOUND,
            SERVER_ERROR
        }

        enum class TripFinishApiError {
            ACTIVE_TRIP_NOT_FOUND,
            SERVER_ERROR
        }

        val trips = DriversTripsResource(configurationProvider)

        class DriversTripsResource(private val configurationProvider: ConfigurationProvider) {
            fun start(): Response<TripResponseBody, TripStartApiError> {
                val httpClient = configurationProvider.client
                val jsonConverter = configurationProvider.jsonConverter

                val response = httpClient.post("api/v1/drivers/me/trips", "")

                return if (response.isSuccessful) {
                    val tripResponseBody = jsonConverter
                        .fromJson(response.body?.source!!, TripResponseBody::class)
                    Response.success(tripResponseBody)
                } else {
                    if (response.code in 400..499) {
                        val errorBody = jsonConverter
                            .fromJson(response.body?.source!!, ErrorResponseBody::class)
                            .errors[0]
                        when (errorBody.code) {
                            "noTaxiRequestToStartTripFrom" ->
                                Response.failure(TripStartApiError.NO_TAXI_REQUEST_TO_START_TRIP_FROM)
                            else -> Response.failure(TripStartApiError.SERVER_ERROR)
                        }
                    } else {
                        Response.failure(TripStartApiError.SERVER_ERROR)
                    }
                }
            }

            fun getById(tripId: String): Response<TripResponseBody, TripRetrievalApiError> {
                val httpClient = configurationProvider.client
                val jsonConverter = configurationProvider.jsonConverter

                val response = httpClient.get("api/v1/drivers/me/trips/$tripId")

                return if (response.isSuccessful) {
                    val tripResponseBody = jsonConverter
                        .fromJson(response.body?.source!!, TripResponseBody::class)
                    Response.success(tripResponseBody)
                } else {
                    if (response.code in 400..499) {
                        val errorBody = jsonConverter
                            .fromJson(response.body?.source!!, ErrorResponseBody::class)
                            .errors[0]
                        when (errorBody.code) {
                            "noTripFound" ->
                                Response.failure(TripRetrievalApiError.NO_TRIP_FOUND)
                            else -> Response.failure(TripRetrievalApiError.SERVER_ERROR)
                        }
                    } else {
                        Response.failure(TripRetrievalApiError.SERVER_ERROR)
                    }
                }
            }

            fun getCurrent(): Response<TripResponseBody, TripRetrievalApiError> {
                val httpClient = configurationProvider.client
                val jsonConverter = configurationProvider.jsonConverter

                val response = httpClient.get("api/v1/drivers/me/trips/current")

                return if (response.isSuccessful) {
                    val tripResponseBody = jsonConverter
                        .fromJson(response.body?.source!!, TripResponseBody::class)
                    Response.success(tripResponseBody)
                } else {
                    if (response.code in 400..499) {
                        val errorBody = jsonConverter
                            .fromJson(response.body?.source!!, ErrorResponseBody::class)
                            .errors[0]
                        when (errorBody.code) {
                            "noActiveTripFound" ->
                                Response.failure(TripRetrievalApiError.NO_TRIP_FOUND)
                            else -> Response.failure(TripRetrievalApiError.SERVER_ERROR)
                        }
                    } else {
                        Response.failure(TripRetrievalApiError.SERVER_ERROR)
                    }
                }
            }

            fun addToRoute(locations: List<LocationRequestBody>): Response<TripResponseBody, LocationToRouteApiError> {
                val httpClient = configurationProvider.client
                val jsonConverter = configurationProvider.jsonConverter

                val jsonBody = jsonConverter.toJson(
                    locations,
                    classOf<List<LocationRequestBody>>(),
                    List::class.java,
                    LocationRequestBody::class.java
                )

                val response = httpClient.patch(
                    "api/v1/drivers/me/trips/current/route",
                    jsonBody
                )

                return if (response.isSuccessful) {
                    val tripResponseBody = jsonConverter
                        .fromJson(response.body?.source!!, TripResponseBody::class)
                    Response.success(tripResponseBody)
                } else {
                    if (response.code in 400..499) {
                        val errorBody = jsonConverter
                            .fromJson(response.body?.source!!, ErrorResponseBody::class)
                            .errors[0]
                        when (errorBody.code) {
                            "noActiveTripFound" ->
                                Response.failure(LocationToRouteApiError.ACTIVE_TRIP_NOT_FOUND)
                            else -> Response.failure(LocationToRouteApiError.SERVER_ERROR)
                        }
                    } else {
                        Response.failure(LocationToRouteApiError.SERVER_ERROR)
                    }
                }
            }

            fun finishCurrent(): Response<TripResponseBody, TripFinishApiError> {
                val httpClient = configurationProvider.client
                val jsonConverter = configurationProvider.jsonConverter

                val response = httpClient.patch("api/v1/drivers/me/trips/current/finish")

                return if (response.isSuccessful) {
                    val tripResponseBody = jsonConverter
                        .fromJson(response.body?.source!!, TripResponseBody::class)
                    Response.success(tripResponseBody)
                } else {
                    if (response.code in 400..499) {
                        val errorBody = jsonConverter
                            .fromJson(response.body?.source!!, ErrorResponseBody::class)
                            .errors[0]
                        when (errorBody.code) {
                            "noActiveTripFound" ->
                                Response.failure(TripFinishApiError.ACTIVE_TRIP_NOT_FOUND)
                            else -> Response.failure(TripFinishApiError.SERVER_ERROR)
                        }
                    } else {
                        Response.failure(TripFinishApiError.SERVER_ERROR)
                    }
                }
            }

            private inline fun <reified T: Any> classOf() = T::class
        }

        fun setAsOnline(): Response<Nothing, OnlineSwitchApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.patch("api/v1/drivers/me/status/online")

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]
                    when (errorBody.code) {
                        "noDeviceRegistered" -> Response.failure(OnlineSwitchApiError.NO_DEVICE_REGISTERED)
                        else -> Response.failure(OnlineSwitchApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(OnlineSwitchApiError.SERVER_ERROR)
                }
            }
        }

        fun setAsOffline(): Response<Nothing, String> {
            val httpClient = configurationProvider.client

            val response = httpClient.patch("api/v1/drivers/me/status/offline")

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                Response.failure(response.body?.string() ?: "")
            }
        }

        fun getTaxiRequestById(taxiRequestId: String): Response<TaxiRequestResponseBody, TaxiRetrievalApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.get("api/v1/drivers/me/taxi-requests/$taxiRequestId")

            return if (response.isSuccessful) {
                val taxiRequestResponseBody = jsonConverter
                    .fromJson(response.body?.source!!, TaxiRequestResponseBody::class)
                Response.success(taxiRequestResponseBody)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]
                    when (errorBody.code) {
                        "taxiRequestNotFound" ->
                            Response.failure(TaxiRetrievalApiError.TAXI_REQUEST_NOT_FOUND)
                        else -> Response.failure(TaxiRetrievalApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(TaxiRetrievalApiError.SERVER_ERROR)
                }
            }
        }

        fun getCurrentTaxiRequest(): Response<TaxiRequestResponseBody, TaxiRetrievalApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.get("api/v1/drivers/me/taxi-requests/current")

            return if (response.isSuccessful) {
                val taxiRequestResponseBody = jsonConverter
                    .fromJson(response.body?.source!!, TaxiRequestResponseBody::class)
                Response.success(taxiRequestResponseBody)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]
                    when (errorBody.code) {
                        "taxiRequestNotFound" ->
                            Response.failure(TaxiRetrievalApiError.TAXI_REQUEST_NOT_FOUND)
                        else -> Response.failure(TaxiRetrievalApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(TaxiRetrievalApiError.SERVER_ERROR)
                }
            }
        }

        fun acceptTaxiRequest(taxiRequestId: String): Response<Nothing, TaxiRequestAcceptanceApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.patch(
                "api/v1/drivers/me/taxi-requests/$taxiRequestId/accept"
            )

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]
                    when (errorBody.code) {
                        "taxiRequestAlreadyAccepted" ->
                            Response.failure(TaxiRequestAcceptanceApiError.ALREADY_ACCEPTED_BY_YOU)
                        "taxiRequestAlreadyAcceptedByAnotherDriver" ->
                            Response.failure(TaxiRequestAcceptanceApiError.ALREADY_ACCEPTED_BY_ANOTHER_DRIVER)
                        "taxiRequestExpired" ->
                            Response.failure(TaxiRequestAcceptanceApiError.EXPIRED)
                        "taxiRequestNotFound" ->
                            Response.failure(TaxiRequestAcceptanceApiError.NOT_FOUND)
                        else -> Response.failure(TaxiRequestAcceptanceApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(TaxiRequestAcceptanceApiError.SERVER_ERROR)
                }
            }
        }

        fun announceArrival(): Response<Nothing, TaxiRequestAnnounceApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.patch(
                "api/v1/drivers/me/taxi-requests/current/announce-arrival"
            )

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]

                    when (errorBody.code) {
                        "taxiRequestNotInAcceptedStatus" ->
                            Response.failure(TaxiRequestAnnounceApiError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS)
                        "taxiRequestNotFound" ->
                            Response.failure(TaxiRequestAnnounceApiError.NOT_FOUND)
                        else ->
                            Response.failure(TaxiRequestAnnounceApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(TaxiRequestAnnounceApiError.SERVER_ERROR)
                }
            }
        }

        fun cancelCurrent(): Response<Nothing, TaxiRequestCancellationApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.patch(
                "api/v1/drivers/me/taxi-requests/current/cancel"
            )

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]

                    when (errorBody.code) {
                        "taxiRequestNotFound" ->
                            Response.failure(TaxiRequestCancellationApiError.NOT_FOUND)
                        else ->
                            Response.failure(TaxiRequestCancellationApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(TaxiRequestCancellationApiError.SERVER_ERROR)
                }
            }
        }
    }
}