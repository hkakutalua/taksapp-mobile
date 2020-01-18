package com.taksapp.taksapp.data.repositories

import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.PushNotificationTokenRetriever
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.TaxiRequest
import java.io.IOException

enum class CreateTaxiRequestError {
    NO_AVAILABLE_DRIVERS,
    SERVER_ERROR
}

enum class UpdateAsCancelledError {

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
     * Updates current taxi request as cancelled
     * @return a [Result] containing the [TaxiRequest] if successful
     * @throws [IOException] if a network error occurs
     */
    suspend fun updateCurrentAsCancelled(): Result<Nothing, UpdateAsCancelledError> {
        ridersTaxiRequestService.cancelCurrentTaxiRequest()
        return Result.success(null)
    }

    private fun hasTaxiRequestReachedMaxTries(
        tries: Int,
        taxiRequestResult: Result<TaxiRequest, TaxiRequestError>
    ): Boolean {
        val maxTries = 2
        return tries >= maxTries && taxiRequestResult.hasFailed
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