package com.taksapp.taksapp.data.webservices.client.resources.users.requests

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.ErrorResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.SignUpError

class SignUpRequest(
    private val phoneNumber: String,
    private val firstName: String,
    private val lastName: String,
    private val password: String,
    private val configurationProvider: ConfigurationProvider) {

    class Builder(private val configurationProvider: ConfigurationProvider) {
        private var phoneNumber = ""
        private var firstName = ""
        private var lastName = ""
        private var password = ""

        fun phoneNumber(phoneNumber: String): Builder {
            this.phoneNumber = phoneNumber
            return this
        }

        fun firstName(firstName: String): Builder {
            this.firstName = firstName
            return this
        }

        fun lastName(lastName: String): Builder {
            this.lastName = lastName
            return this
        }

        fun password(password: String): Builder {
            this.password = password
            return this
        }

        fun build(): SignUpRequest {
            return SignUpRequest(
                phoneNumber = this.phoneNumber,
                firstName = this.firstName,
                lastName = this.lastName,
                password = this.password,
                configurationProvider = this.configurationProvider
            )
        }
    }

    internal data class PhoneSignUpBody(
        val firstName: String,
        val lastName: String,
        val password: String,
        val phoneNumber: String
    )

    internal data class SuccessfulSignUpBody(val otpId: String)

    /**
     * Starts the sign-up process by OTP confirmation
     * @return a successful response with the OTP id southWest use for verification
     */
    fun signUp(): Response<String, List<SignUpError>> {
        val client = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        val requestBody = PhoneSignUpBody(
            firstName = this.firstName,
            lastName = this.lastName,
            password = this.password,
            phoneNumber = this.phoneNumber
        )

        val response = client.post(
            "api/v1/users/start-phone-sign-up",
            jsonConverter.toJson(requestBody))

        return if (response.isSuccessful) {
            val body = jsonConverter.fromJson(response.body!!.source!!, SuccessfulSignUpBody::class)
            Response.success(body.otpId)
        } else {
            if (response.code in 400..499) {
                val responseBody = jsonConverter
                    .fromJson(response.body!!.source!!, ErrorResponseBody::class)

                val mappedErrors = responseBody.errors.map { error ->
                    when {
                        error.code == "phoneNumberAlreadyRegistered" ->
                            SignUpError.PHONE_NUMBER_ALREADY_REGISTERED
                        else -> throw Exception("Unknown code ${error.code}")
                    }
                }

                Response.failure(mappedErrors)
            } else {
                throw InternalServerErrorException(response.body?.string().orEmpty())
            }
        }
    }
}