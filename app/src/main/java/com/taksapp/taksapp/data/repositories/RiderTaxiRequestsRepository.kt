package com.taksapp.taksapp.data.repositories

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.*
import java.io.IOException

enum class CreateTaxiRequestError {
    NO_AVAILABLE_DRIVERS,
    SERVER_ERROR
}

enum class GetTaxiRequestError {
    NO_TAXI_REQUEST,
    SERVER_ERROR
}

enum class CancelTaxiRequestError {
    NO_TAXI_REQUEST,
    SERVER_ERROR
}

class RiderTaxiRequestsRepository(
    private val ridersTaxiRequestService: RidersTaxiRequestService,
    private val devicesService: DevicesService,
    private val pushNotificationTokenRetriever: PushNotificationTokenRetriever
) {
    /**
     * Creates a taxi request for rider
     * @return a [Result] containing the [TaxiRequest] if successful
     * @throws [IOException] if a network error occurs
     */
    suspend fun create(
        origin: Location,
        destination: Location
    ): Result<TaxiRequest, CreateTaxiRequestError> {
        var triesCount = 0

        while (true) {
            val taxiRequestResult = ridersTaxiRequestService.sendTaxiRequest(origin, destination)
            triesCount++

            if (hasTaxiRequestReachedMaxTries(triesCount, taxiRequestResult)) {
                return Result.error(CreateTaxiRequestError.SERVER_ERROR)
            }

            if (taxiRequestResult.isSuccessful) {
                return Result.success(taxiRequestResult.data)
            } else if (hasFailedDueToDeviceNotRegistered(taxiRequestResult)) {
                val registrationResult = registerUserDevice()
                if (registrationResult.isSuccessful)
                    continue
            } else if (failedDueToNoAvailableDrivers(taxiRequestResult)) {
                return Result.error(CreateTaxiRequestError.NO_AVAILABLE_DRIVERS)
            } else if (anActiveTaxiRequestExists(taxiRequestResult)) {
                val currentTaxiRequestResult = ridersTaxiRequestService.getCurrentTaxiRequest()
                if (currentTaxiRequestResult.isSuccessful) {
                    return Result.success(currentTaxiRequestResult.data)
                }
            } else {
                return Result.error(CreateTaxiRequestError.SERVER_ERROR)
            }
        }
    }

    /**
     * Gets the rider's current taxi request
     * @return a [Result] containing the [TaxiRequest] if successful
     * @throws [IOException] if a network error occurs
     */
    suspend fun getCurrent(): Result<TaxiRequest, GetTaxiRequestError> {
        val taxiRequestResult = ridersTaxiRequestService.getCurrentTaxiRequest()
        return if (taxiRequestResult.isSuccessful) {
            Result.success(taxiRequestResult.data)
        } else {
            when (taxiRequestResult.error) {
                TaxiRequestRetrievalError.NOT_FOUND -> Result.error(GetTaxiRequestError.NO_TAXI_REQUEST)
                TaxiRequestRetrievalError.SERVER_ERROR -> Result.error(GetTaxiRequestError.SERVER_ERROR)
                else -> Result.error(GetTaxiRequestError.SERVER_ERROR)
            }
        }
    }

    /**
     * Gets the rider's taxi request by its id
     * @return a [Result] containing the [TaxiRequest] if successful
     * @throws [IOException] if a network error occurs
     */
    suspend fun getById(taxiRequestId: String): Result<TaxiRequest, GetTaxiRequestError> {
        val taxiRequestResult = ridersTaxiRequestService.getTaxiRequestById(taxiRequestId)
        return if (taxiRequestResult.isSuccessful) {
            Result.success(taxiRequestResult.data)
        } else {
            when (taxiRequestResult.error) {
                TaxiRequestRetrievalError.NOT_FOUND -> Result.error(GetTaxiRequestError.NO_TAXI_REQUEST)
                TaxiRequestRetrievalError.SERVER_ERROR -> Result.error(GetTaxiRequestError.SERVER_ERROR)
                else -> Result.error(GetTaxiRequestError.SERVER_ERROR)
            }
        }
    }

    /**
     * Updates current taxi request as cancelled
     * @return a [Result] containing the [TaxiRequest] if successful
     * @throws [IOException] if a network error occurs
     */
    suspend fun updateCurrentAsCancelled(): Result<Nothing, CancelTaxiRequestError> {
        val result = ridersTaxiRequestService.cancelCurrentTaxiRequest()
        return if (result.isSuccessful) {
            return Result.success(null)
        } else {
            when (result.error) {
                CancellationError.TAXI_REQUEST_NOT_FOUND -> Result.error(CancelTaxiRequestError.NO_TAXI_REQUEST)
                CancellationError.SERVER_ERROR -> Result.error(CancelTaxiRequestError.SERVER_ERROR)
                else -> Result.error(CancelTaxiRequestError.SERVER_ERROR)
            }
        }
    }

    private fun hasTaxiRequestReachedMaxTries(
        tries: Int,
        taxiRequestResult: Result<TaxiRequest, TaxiRequestError>
    ): Boolean {
        val maxTries = 2
        return tries > maxTries && taxiRequestResult.hasFailed
    }

    private fun anActiveTaxiRequestExists(taxiRequestResult: Result<TaxiRequest, TaxiRequestError>) =
        taxiRequestResult.hasFailed &&
                taxiRequestResult.error == TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS

    private suspend fun registerUserDevice(): Result<Nothing, String> {
        val tokenResult = pushNotificationTokenRetriever.getPushNotificationToken()
        if (tokenResult.hasFailed)
            return Result.error(tokenResult.error)

        return devicesService.registerUserDevice(
            tokenResult.data!!,
            DevicesService.Platform.ANDROID
        )
    }

    private fun failedDueToNoAvailableDrivers(taxiRequestResult: Result<TaxiRequest, TaxiRequestError>) =
        taxiRequestResult.hasFailed &&
                taxiRequestResult.error == TaxiRequestError.NO_AVAILABLE_DRIVERS

    private fun hasFailedDueToDeviceNotRegistered(taxiRequestResult: Result<TaxiRequest, TaxiRequestError>) =
        taxiRequestResult.hasFailed &&
                taxiRequestResult.error == TaxiRequestError.DEVICE_NOT_REGISTERED
}