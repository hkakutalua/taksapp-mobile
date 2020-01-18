package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.resources.riders.CancellationApiError
import com.taksapp.taksapp.data.webservices.client.resources.riders.TaxiRequestApiError
import com.taksapp.taksapp.data.webservices.client.resources.riders.TaxiRequestRequestBody
import com.taksapp.taksapp.data.webservices.client.resources.riders.TaxiRequestStatus
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.domain.interfaces.TaxiRequestRetrievalError

class RidersTaxiRequestWebService(private val taksapp: Taksapp) : RidersTaxiRequestService {
    override suspend fun sendTaxiRequest(
        origin: Location,
        destination: Location
    ): Result<TaxiRequest, TaxiRequestError> {
        val originBody =
            com.taksapp.taksapp.data.webservices.client.resources.common.Location(0.2034, 0.2034)
        val destinationBody =
            com.taksapp.taksapp.data.webservices.client.resources.common.Location(0.2034, 0.2034)
        val requestBody = TaxiRequestRequestBody(originBody, destinationBody)

        val response = taksapp.riders.taxiRequests.create(requestBody)
        return if (response.successful) {
            val taxiRequest = TaxiRequest(
                when (response.data?.status) {
                    TaxiRequestStatus.waitingAcceptance -> Status.WAITING_ACCEPTANCE
                    TaxiRequestStatus.accepted -> Status.ACCEPTED
                    TaxiRequestStatus.driverArrived -> Status.DRIVER_ARRIVED
                    TaxiRequestStatus.cancelled -> Status.CANCELLED
                    else -> throw Exception("Unknown status: ${response.data?.status}")
                }
            )

            Result.success(taxiRequest)
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

    override suspend fun cancelCurrentTaxiRequest(): Result<Nothing, CancellationError> {
        val response = taksapp.riders.taxiRequests.cancelCurrent()
        return if (response.successful) {
            Result.success(null)
        } else {
            when (response.error) {
                CancellationApiError.NOT_FOUND -> Result.error(CancellationError.TAXI_REQUEST_NOT_FOUND)
                CancellationApiError.SERVER_ERROR -> Result.error(CancellationError.SERVER_ERROR)
                else -> Result.error(CancellationError.SERVER_ERROR)
            }
        }
    }

    override suspend fun getCurrentTaxiRequest(): Result<TaxiRequest, TaxiRequestRetrievalError> {
        return Result.error(null)
    }
}