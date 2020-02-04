package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource.CurrentDriverResource
import com.taksapp.taksapp.domain.interfaces.DriversService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriversWebService(private val taksapp: Taksapp) : DriversService {
    override suspend fun setAsOnline(): Result<Nothing, DriversService.OnlineSwitchError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.setAsOnline()
            return@withContext if (result.successful) {
                Result.success<Nothing, DriversService.OnlineSwitchError>(null)
            } else {
                when (result.error) {
                    CurrentDriverResource.OnlineSwitchApiError.NO_DEVICE_REGISTERED ->
                        Result.error(DriversService.OnlineSwitchError.DRIVER_HAS_NO_DEVICE)
                    CurrentDriverResource.OnlineSwitchApiError.SERVER_ERROR ->
                        Result.error(DriversService.OnlineSwitchError.SERVER_ERROR)
                    null -> Result.error(DriversService.OnlineSwitchError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun setAsOffline(): Result<Nothing, String> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.setAsOffline()
            return@withContext if (result.successful) {
                Result.success<Nothing,String>(null)
            } else {
                Result.error(result.error)
            }
        }
    }
}