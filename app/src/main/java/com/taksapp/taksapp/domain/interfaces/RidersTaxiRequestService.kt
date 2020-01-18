package com.taksapp.taksapp.domain.interfaces

import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.TaxiRequest
import java.io.IOException

enum class TaxiRequestError {
    NO_AVAILABLE_DRIVERS,
    DEVICE_NOT_REGISTERED,
    ACTIVE_TAXI_REQUEST_EXISTS,
    SERVER_ERROR
}

enum class TaxiRequestRetrievalError {}

enum class CancellationError {
    TAXI_REQUEST_NOT_FOUND,
    SERVER_ERROR
}

interface RidersTaxiRequestService {
    /**
     * Sends a taxi request
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun sendTaxiRequest(
        origin: Location,
        destination: Location
    ): Result<TaxiRequest, TaxiRequestError>

    /**
     * Cancels rider's current taxi request
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun cancelCurrentTaxiRequest(): Result<Nothing, CancellationError>

    /**
     * Gets rider's current taxi request
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    suspend fun getCurrentTaxiRequest(): Result<TaxiRequest, TaxiRequestRetrievalError>
}