package com.taksapp.taksapp.domain.interfaces

import com.taksapp.taksapp.application.arch.utils.Result
import java.io.IOException

interface DriversService {
    enum class OnlineSwitchError { DRIVER_HAS_NO_DEVICE, SERVER_ERROR }

    /**
     * Sets the current driver's status as online
     * @return a [Result] indicating the success status.
     * @throws IOException when an internet failure occurs.
     */
    @Throws(IOException::class)
    suspend fun setAsOnline(): Result<Nothing, OnlineSwitchError>

    /**
     * Sets the current driver's status as online
     * @return a [Result] indicating the success status.
     * @throws IOException when an internet failure occurs.
     */
    @Throws(IOException::class)
    suspend fun setAsOffline(): Result<Nothing, String>
}