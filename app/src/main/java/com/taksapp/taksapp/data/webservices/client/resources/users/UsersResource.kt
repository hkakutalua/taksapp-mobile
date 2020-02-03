package com.taksapp.taksapp.data.webservices.client.resources.users

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.LoginRequest
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.SignUpOtpConfirmationRequest
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.SignUpRequest

enum class Platform { android }
data class DeviceUpdateRequestBody(val pushNotificationToken: String, val platform: Platform)

class UsersResource(
    private val configurationProvider: ConfigurationProvider,
    private val store: SessionStore
) {
    fun loginRequestBuilder(): LoginRequest.Builder {
        return LoginRequest.Builder(configurationProvider, store)
    }

    fun signUpRequestBuilder(): SignUpRequest.Builder {
        return SignUpRequest.Builder(configurationProvider)
    }

    fun signUpOtpConfirmationBuilder(): SignUpOtpConfirmationRequest.Builder {
        return SignUpOtpConfirmationRequest.Builder(configurationProvider)
    }

    fun updateDevice(requestBody: DeviceUpdateRequestBody): Response<Nothing, String> {
        val httpClient = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        val bodyJson = jsonConverter.toJson(requestBody)

        val response = httpClient.put("api/v1/users/me/devices/unique", bodyJson)
        return if (response.isSuccessful) {
            Response.success(null)
        } else {
            Response.failure(response.body?.string() ?: "")
        }
    }
}