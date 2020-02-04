package com.taksapp.taksapp.data.webservices.client.resources.drivers

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.resources.common.ErrorResponseBody

class DriversResource(private val configurationProvider: ConfigurationProvider) {
    val me = CurrentDriverResource(configurationProvider)

    class CurrentDriverResource(private val configurationProvider: ConfigurationProvider) {
        enum class OnlineSwitchApiError { NO_DEVICE_REGISTERED, SERVER_ERROR }

        fun setAsOnline(): Response<Nothing, OnlineSwitchApiError> {
            val httpClient = configurationProvider.client
            val jsonConverter = configurationProvider.jsonConverter

            val response = httpClient.patch("api/v1/drivers/me/status/online")

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                if (response.code in 400..499) {
                    val errorBody = jsonConverter
                        .fromJson(response.body?.source!!, ErrorResponseBody::class)
                        .errors[0]
                    when (errorBody.code) {
                        "noDeviceRegistered" -> Response.failure(OnlineSwitchApiError.NO_DEVICE_REGISTERED)
                        else -> Response.failure(OnlineSwitchApiError.SERVER_ERROR)
                    }
                } else {
                    Response.failure(OnlineSwitchApiError.SERVER_ERROR)
                }
            }
        }

        fun setAsOffline(): Response<Nothing, String> {
            val httpClient = configurationProvider.client

            val response = httpClient.patch("api/v1/drivers/me/status/offline")

            return if (response.isSuccessful) {
                Response.success(null)
            } else {
                Response.failure(response.body?.string() ?: "")
            }
        }
    }
}