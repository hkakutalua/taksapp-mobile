package com.taksapp.taksapp.data.webservices.client.resources.common.requests

import com.taksapp.taksapp.data.webservices.client.ApiErrorBody
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.errors.LoginRequestError

abstract class LoginRequest(
        private val email: String,
        private val password: String,
        private val pushNotificationToken: String,
        protected val configurationProvider: ConfigurationProvider,
        protected val store: SessionStore) {

    abstract class BaseBuilder {

        protected var email = ""
        protected var password = ""
        protected var pushNotificationToken = ""

        fun email(email: String): BaseBuilder {
            this.email = email
            return this
        }

        fun password(password: String): BaseBuilder {
            this.password = password
            return this
        }

        fun pushNotificationToken(pushNotificationToken: String): BaseBuilder {
            this.pushNotificationToken = pushNotificationToken
            return this
        }

        abstract fun build(): LoginRequest
    }

    internal data class LoginRequestBody(val email: String, val password: String, val pushNotificationToken: String)
    internal data class LoginResponseBody(val accessToken: String, val refreshToken: String)

    abstract val endpoint: String

    abstract fun saveUserType()

    fun login(): Response<Void, LoginRequestError> {
        val jsonConverter = configurationProvider.jsonConverter
        val client = configurationProvider.client

        val loginBodyJson = jsonConverter.toJson(LoginRequestBody(email, password, pushNotificationToken))

        val response = client.post(endpoint, loginBodyJson)

        return if (response.isSuccessful) {
            val responseBody = jsonConverter.fromJson(response.body!!.source!!, LoginResponseBody::class)

            store.saveAccessToken(responseBody.accessToken)
            store.saveRefreshToken(responseBody.refreshToken)
            saveUserType()

            Response.success(null)

        } else {
            if (response.code < 500 && response.body != null) {
                val apiErrorBody = jsonConverter.fromJson(response.body.source!!, ApiErrorBody::class)
                Response.failure(LoginRequestError.of(apiErrorBody.errorCode))
            } else {
                throw InternalServerErrorException(response.body!!.string()!!)
            }
        }
    }
}