package com.taksapp.taksapp.domain.interfaces

import com.taksapp.taksapp.arch.utils.Result

interface DevicesService {
    enum class Platform { ANDROID }

    /**
     * Registers the device of the current user
     */
    fun registerUserDevice(
        pushNotificationToken: String,
        platform: Platform
    ): Result<Nothing, String>
}