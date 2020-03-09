package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.TaxiRequestResponseBodyMapper
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource.CurrentDriverResource.*
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriversTaxiRequestWebService(private val taksapp: Taksapp) : DriversTaxiRequestService {
    override suspend fun getTaxiRequestById(id: String): Result<TaxiRequest, TaxiRequestRetrievalError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.getTaxiRequestById(id)
            return@withContext if (result.successful) {
                Result.success<TaxiRequest, TaxiRequestRetrievalError>(
                    TaxiRequestResponseBodyMapper.mapToTaxiRequest(result.data!!))
            } else {
                when (result.error) {
                    TaxiRetrievalApiError.TAXI_REQUEST_NOT_FOUND ->
                        Result.error(TaxiRequestRetrievalError.NOT_FOUND)
                    TaxiRetrievalApiError.SERVER_ERROR ->
                        Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun getCurrentTaxiRequest(): Result<TaxiRequest, TaxiRequestRetrievalError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.getCurrentTaxiRequest()
            return@withContext if (result.successful) {
                Result.success<TaxiRequest, TaxiRequestRetrievalError>(
                    TaxiRequestResponseBodyMapper.mapToTaxiRequest(result.data!!))
            } else {
                when (result.error) {
                    TaxiRetrievalApiError.TAXI_REQUEST_NOT_FOUND ->
                        Result.error(TaxiRequestRetrievalError.NOT_FOUND)
                    TaxiRetrievalApiError.SERVER_ERROR ->
                        Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun acceptTaxiRequest(id: String): Result<Nothing, TaxiRequestAcceptanceError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.acceptTaxiRequest(id)
            return@withContext if (result.successful) {
                Result.success<Nothing, TaxiRequestAcceptanceError>(null)
            } else {
                when (result.error) {
                    TaxiRequestAcceptanceApiError.ALREADY_ACCEPTED_BY_YOU ->
                        Result.error(TaxiRequestAcceptanceError.TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU)
                    TaxiRequestAcceptanceApiError.ALREADY_ACCEPTED_BY_ANOTHER_DRIVER ->
                        Result.error(TaxiRequestAcceptanceError.TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER)
                    TaxiRequestAcceptanceApiError.EXPIRED ->
                        Result.error(TaxiRequestAcceptanceError.TAXI_REQUEST_EXPIRED)
                    TaxiRequestAcceptanceApiError.NOT_FOUND ->
                        Result.error(TaxiRequestAcceptanceError.TAXI_REQUEST_NOT_FOUND)
                    TaxiRequestAcceptanceApiError.SERVER_ERROR ->
                        Result.error(TaxiRequestAcceptanceError.SERVER_ERROR)
                    else -> Result.error(TaxiRequestAcceptanceError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun announceArrival(): Result<Nothing, TaxiRequestArrivalAnnounceError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.announceArrival()

            return@withContext if (result.successful) {
                Result.success<Nothing, TaxiRequestArrivalAnnounceError>(null)
            } else {
                when (result.error) {
                    TaxiRequestAnnounceApiError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS ->
                        Result.error(TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS)
                    TaxiRequestAnnounceApiError.NOT_FOUND ->
                        Result.error(TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_FOUND)
                    TaxiRequestAnnounceApiError.SERVER_ERROR ->
                        Result.error(TaxiRequestArrivalAnnounceError.SERVER_ERROR)
                    null -> Result.error(TaxiRequestArrivalAnnounceError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun cancelCurrentTaxiRequest(): Result<Nothing, TaxiRequestCancellationError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.cancelCurrent()

            return@withContext if (result.successful) {
                Result.success<Nothing, TaxiRequestCancellationError>(null)
            } else {
                when (result.error) {
                    TaxiRequestCancellationApiError.NOT_FOUND ->
                        Result.error(TaxiRequestCancellationError.TAXI_REQUEST_NOT_FOUND)
                    TaxiRequestCancellationApiError.SERVER_ERROR ->
                        Result.error(TaxiRequestCancellationError.SERVER_ERROR)
                    null -> Result.error(TaxiRequestCancellationError.SERVER_ERROR)
                }
            }
        }
    }
}