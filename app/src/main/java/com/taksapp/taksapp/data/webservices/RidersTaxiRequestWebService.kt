package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.resources.common.LocationResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.riders.*
import com.taksapp.taksapp.domain.*
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.domain.interfaces.TaxiRequestRetrievalError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RidersTaxiRequestWebService(private val taksapp: Taksapp) : RidersTaxiRequestService {
    override suspend fun sendTaxiRequest(
        origin: Location,
        destination: Location
    ): Result<TaxiRequest, TaxiRequestError> {
        return withContext(Dispatchers.IO) {
            val originBody = LocationResponseBody(origin.latitude, origin.longitude)
            val destinationBody = LocationResponseBody(destination.latitude, destination.longitude)
            val requestBody = TaxiRequestRequestBody(originBody, destinationBody)

            val response = taksapp.riders.taxiRequests.create(requestBody)
            return@withContext if (response.successful) {
                val taxiRequest = mapToTaxiRequest(response.data!!)
                Result.success<TaxiRequest, TaxiRequestError>(taxiRequest)
            } else {
                when (response.error) {
                    TaxiRequestApiError.RIDER_HAS_NO_REGISTERED_DEVICE -> Result.error(TaxiRequestError.DEVICE_NOT_REGISTERED)
                    TaxiRequestApiError.NO_AVAILABLE_DRIVERS -> Result.error(TaxiRequestError.NO_AVAILABLE_DRIVERS)
                    TaxiRequestApiError.EXISTING_ACTIVE_TAXI_REQUEST -> Result.error(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS)
                    TaxiRequestApiError.SERVER_ERROR -> Result.error(TaxiRequestError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun cancelCurrentTaxiRequest(): Result<Nothing, CancellationError> {
        return withContext(Dispatchers.IO) {
            val response = taksapp.riders.taxiRequests.cancelCurrent()
            return@withContext if (response.successful) {
                Result.success<Nothing, CancellationError>(null)
            } else {
                when (response.error) {
                    CancellationApiError.NOT_FOUND -> Result.error(CancellationError.TAXI_REQUEST_NOT_FOUND)
                    CancellationApiError.SERVER_ERROR -> Result.error(CancellationError.SERVER_ERROR)
                    else -> Result.error(CancellationError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun getCurrentTaxiRequest(): Result<TaxiRequest, TaxiRequestRetrievalError> {
        return withContext(Dispatchers.IO) {
            val response = taksapp.riders.taxiRequests.getCurrent()
            return@withContext if (response.successful) {
                val taxiRequest = mapToTaxiRequest(response.data!!)
                Result.success<TaxiRequest, TaxiRequestRetrievalError>(taxiRequest)
            } else {
                when (response.error) {
                    TaxiRequestFetchApiError.NOT_FOUND -> Result.error(TaxiRequestRetrievalError.NOT_FOUND)
                    TaxiRequestFetchApiError.SERVER_ERROR -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun getTaxiRequestById(id: String): Result<TaxiRequest, TaxiRequestRetrievalError> {
        return withContext(Dispatchers.IO) {
            val response = taksapp.riders.taxiRequests.getById(id)
            return@withContext if (response.successful) {
                val taxiRequest =  mapToTaxiRequest(response.data!!)
                Result.success<TaxiRequest, TaxiRequestRetrievalError>(taxiRequest)
            } else {
                when (response.error) {
                    TaxiRequestFetchApiError.NOT_FOUND -> Result.error(TaxiRequestRetrievalError.NOT_FOUND)
                    TaxiRequestFetchApiError.SERVER_ERROR -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    private fun mapToTaxiRequest(taxiRequestResponseBody: TaxiRequestResponseBody): TaxiRequest {
        return TaxiRequest.withBuilder()
            .withId(taxiRequestResponseBody.id)
            .withOrigin(
                mapToLocation(taxiRequestResponseBody.origin),
                taxiRequestResponseBody.originLocationName
            )
            .withDestination(
                mapToLocation(taxiRequestResponseBody.destination),
                taxiRequestResponseBody.destinationLocationName
            )
            .withRider(mapToRider(taxiRequestResponseBody.rider))
            .withOptionalDriver(mapToNullableDriver(taxiRequestResponseBody.driver))
            .withExpirationDate(taxiRequestResponseBody.expirationDate)
            .withStatus(mapTaxiRequestStatus(taxiRequestResponseBody.status))
            .build()
    }

    private fun mapToRider(rider: RiderResponseBody) =
        Rider(rider.id, rider.firstName, rider.lastName, mapToNullableLocation(rider.location))

    private fun mapToNullableDriver(driver: DriverResponseBody?) =
        if (driver != null)
            Driver(
                driver.id,
                driver.firstName,
                driver.lastName,
                mapToNullableLocation(driver.location)
            )
        else null

    private fun mapToLocation(sourceLocation: LocationResponseBody) =
        Location(sourceLocation.latitude, sourceLocation.longitude)

    private fun mapToNullableLocation(sourceLocation: LocationResponseBody?) =
        if (sourceLocation != null)
            Location(sourceLocation.latitude, sourceLocation.longitude)
        else null

    private fun mapTaxiRequestStatus(taxiRequestStatus: TaxiRequestStatus): Status {
        return when (taxiRequestStatus) {
            TaxiRequestStatus.waitingAcceptance -> Status.WAITING_ACCEPTANCE
            TaxiRequestStatus.accepted -> Status.ACCEPTED
            TaxiRequestStatus.driverArrived -> Status.DRIVER_ARRIVED
            TaxiRequestStatus.cancelled -> Status.CANCELLED
        }
    }
}