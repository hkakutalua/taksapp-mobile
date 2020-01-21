package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.resources.users.DeviceUpdateRequestBody
import com.taksapp.taksapp.data.webservices.client.resources.users.Platform
import com.taksapp.taksapp.domain.interfaces.DevicesService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DevicesWebService(private val taksapp: Taksapp): DevicesService {
    override suspend fun registerUserDevice(
        pushNotificationToken: String,
        platform: DevicesService.Platform
    ): Result<Nothing, String> {
        return withContext(Dispatchers.IO) {
            val requestBody = DeviceUpdateRequestBody(pushNotificationToken, Platform.android)
            val response = taksapp.users.updateDevice(requestBody)
            return@withContext if (response.successful) {
                Result.success<Nothing, String>(null)
            } else {
                Result.error(response.error)
            }
        }
    }
}