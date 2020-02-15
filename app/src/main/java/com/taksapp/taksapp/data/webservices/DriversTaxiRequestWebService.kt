package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.TaxiRequestResponseBodyMapper
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource.CurrentDriverResource.TaxiRequestAcceptanceApiError
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource.CurrentDriverResource.TaxiRetrievalApiError
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestAcceptanceError
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestRetrievalError
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

}