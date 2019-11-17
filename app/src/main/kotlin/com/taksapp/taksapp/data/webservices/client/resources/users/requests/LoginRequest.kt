package com.taksapp.taksapp.data.webservices.client.resources.users.requests

import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.UserType
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.exceptions.ValidationErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.ValidationErrorBody
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.LoginRequestError
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.LoginRequest.OAuth2LoginErrorBody.ErrorType
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.LoginRequest.OAuth2LoginErrorBody.InvalidGrantErrorDescriptionType
import java.util.*

class LoginRequest(
    private val phoneNumber: String,
    private val password: String,
    private val role: UserType,
    private val configurationProvider: ConfigurationProvider,
    private val store: SessionStore
) {

    class Builder(
        private val configurationProvider: ConfigurationProvider,
        private val store: SessionStore
    ) {
        private var phoneNumber = ""
        private var password = ""
        private var role = UserType.RIDER

        fun phoneNumber(phoneNumber: String): Builder {
            this.phoneNumber = phoneNumber
            return this
        }

        fun password(password: String): Builder {
            this.password = password
            return this
        }

        fun role(role: UserType): Builder {
            this.role = role
            return this
        }

        fun build(): LoginRequest {
            return LoginRequest(
                phoneNumber = this.phoneNumber,
                password = this.password,
                role = this.role,
                configurationProvider = this.configurationProvider,
                store = this.store
            )
        }
    }

    internal data class OAuth2LoginResponseBody(
        val access_token: String,
        val refresh_token: String,
        val expires_in: Long
    )

    internal data class OAuth2LoginErrorBody(
        val error: String,
        val error_description: String?
    ) {
        class ErrorType {
            companion object {
                const val INVALID_GRANT = "invalid_grant"
            }
        }

        class InvalidGrantErrorDescriptionType {
            companion object {
                const val INVALID_USERNAME_OR_PASSWORD = "invalid_username_or_password"
            }
        }
    }

    fun login(): Response<Void, LoginRequestError> {
        val accountExists = verifyAccountExistence()
        return if (accountExists) {
            loginWithOAuth()
        } else {
            Response.failure(LoginRequestError.ACCOUNT_DOES_NOT_EXISTS)
        }
    }

    private fun loginWithOAuth(): Response<Void, LoginRequestError> {
        val jsonConverter = configurationProvider.jsonConverter
        val client = configurationProvider.client

        val oAuthLoginForm = mapOf(
            "username" to phoneNumber,
            "password" to password,
            "grant_type" to "password",
            "client_id" to "mobile_application"
        )

        val response = client.post("connect/token", oAuthLoginForm)

        if (response.isSuccessful) {
            val responseBody =
                jsonConverter.fromJson(response.body!!.source!!, OAuth2LoginResponseBody::class)

            store.saveAccessToken(responseBody.access_token)
            store.saveRefreshToken(responseBody.refresh_token)
            store.saveUserType(role)

            return Response.success(null)

        } else {
            if (response.code in 400..499 && response.body != null) {
                val errorBody = jsonConverter
                    .fromJson(response.body.source!!, OAuth2LoginErrorBody::class)
                return if (isIncorrectCredentialsError(errorBody)) {
                    Response.failure(LoginRequestError.INVALID_CREDENTIALS)
                } else {
                    Response.failure(LoginRequestError.UNSUPPORTED_CLIENT)
                }
            }

            throw InternalServerErrorException(response.body?.string().orEmpty())
        }
    }

    private fun isIncorrectCredentialsError(errorBody: OAuth2LoginErrorBody) =
        ErrorType.INVALID_GRANT == errorBody.error &&
                InvalidGrantErrorDescriptionType.INVALID_USERNAME_OR_PASSWORD == errorBody.error_description

    private fun verifyAccountExistence(): Boolean {
        val jsonConverter = configurationProvider.jsonConverter
        val client = configurationProvider.client
        val roleName = role.name.toLowerCase(Locale.ENGLISH)

        val response = client.get("api/v1/users/$phoneNumber/verify-existence?role=$roleName")

        return if (response.isSuccessful) {
            true
        } else {
            when {
                response.code == 404 -> false
                response.code in 400..499 -> {
                    val validationErrorBody = jsonConverter
                        .fromJson(response.body!!.source!!, ValidationErrorBody::class)
                    throw ValidationErrorException(validationErrorBody)
                }
                else -> throw InternalServerErrorException(response.body?.string().orEmpty())
            }
        }
    }
}