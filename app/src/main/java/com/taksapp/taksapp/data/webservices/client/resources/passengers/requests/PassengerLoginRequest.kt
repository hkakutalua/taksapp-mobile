package com.taksapp.taksapp.data.webservices.client.resources.passengers.requests

import com.taksapp.taksapp.data.webservices.client.ApiErrorBody
import com.taksapp.taksapp.data.webservices.client.AuthenticationTokensStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.passengers.errors.LoginRequestError

class PassengerLoginRequest(
    private val email: String,
    private val password: String,
    private val pushNotificationToken: String,
    private val configurationProvider: ConfigurationProvider,
    private val store: AuthenticationTokensStore) {

    class Builder(
        private val configurationProvider: ConfigurationProvider,
        private val store: AuthenticationTokensStore) {

        private var email = ""
        private var password = ""
        private var pushNotificationToken = ""

        fun email(email: String): Builder {
            this.email = email
            return this
        }

        fun password(password: String): Builder {
            this.password = password
            return this
        }

        fun pushNotificationToken(pushNotificationToken: String): Builder {
            this.pushNotificationToken = pushNotificationToken
            return this
        }

        fun build(): PassengerLoginRequest {
            return PassengerLoginRequest(
                email = email,
                password = password,
                pushNotificationToken = pushNotificationToken,
                configurationProvider = configurationProvider,
                store = store
            )
        }
    }

    internal data class LoginRequestBody(val email: String, val password: String, val pushNotificationToken: String)
    internal data class LoginResponseBody(val accessToken: String, val refreshToken: String)

    fun login(): Response<Void, LoginRequestError> {
        val jsonConverter = configurationProvider.jsonConverter
        val client = configurationProvider.client

        val loginBodyJson = jsonConverter
            .toJson(LoginRequestBody(email, password, pushNotificationToken))

        val response = client.post("passengers/login", loginBodyJson)

        return if (response.isSuccessful) {
            val responseBody = jsonConverter.fromJson(response.body!!.source!!, LoginResponseBody::class)

            store.saveAccessToken(responseBody.accessToken)
            store.saveRefreshToken(responseBody.refreshToken)

            Response.success(null)

        } else {
            if (response.code < 500 && response.body != null) {
                val apiErrorBody = jsonConverter.fromJson(response.body.source!!, ApiErrorBody::class)
                Response.failure(LoginRequestError.valueOf(apiErrorBody.errorCode))
            } else {
                throw InternalServerErrorException(response.body!!.string()!!)
            }
        }
    }
}