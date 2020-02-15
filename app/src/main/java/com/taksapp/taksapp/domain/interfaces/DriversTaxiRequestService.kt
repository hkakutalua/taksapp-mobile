package com.taksapp.taksapp.domain.interfaces

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.domain.TaxiRequest
import java.io.IOException

interface DriversTaxiRequestService {
    enum class TaxiRequestRetrievalError {
        NOT_FOUND,
        SERVER_ERROR
    }

    enum class TaxiRequestAcceptanceError {
        TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER,
        TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU,
        TAXI_REQUEST_EXPIRED,
        TAXI_REQUEST_NOT_FOUND,
        SERVER_ERROR
    }

    /**
     * Get taxi request by its [id]
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun getTaxiRequestById(id: String): Result<TaxiRequest, TaxiRequestRetrievalError>

    /**
     * Get driver's current taxi request
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun getCurrentTaxiRequest(): Result<TaxiRequest, TaxiRequestRetrievalError>

    /**
     * Accepts the taxi request by its [id]
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun acceptTaxiRequest(id: String): Result<Nothing, TaxiRequestAcceptanceError>
}