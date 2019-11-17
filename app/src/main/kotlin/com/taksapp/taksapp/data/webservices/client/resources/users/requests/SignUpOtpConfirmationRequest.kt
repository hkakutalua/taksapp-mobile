package com.taksapp.taksapp.data.webservices.client.resources.users.requests

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.ErrorResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.SignUpOtpConfirmationError

class SignUpOtpConfirmationRequest(
    val otpId: String,
    val code: String,
    val configurationProvider: ConfigurationProvider) {

    class Builder(private val configurationProvider: ConfigurationProvider) {
        private var otpId = ""
        private var code = ""

        fun otpId(otpId: String): Builder {
            this.otpId = otpId
            return this
        }

        fun code(code: String): Builder {
            this.code = code
            return this
        }

        fun build(): SignUpOtpConfirmationRequest {
            return SignUpOtpConfirmationRequest(
                otpId = this.otpId,
                code = this.code,
                configurationProvider = this.configurationProvider
            )
        }
    }

    internal data class OtpConfirmationRequestBody(val otpId: String, val code: String)

    fun confirmOtp(): Response<Nothing, List<SignUpOtpConfirmationError>> {
        val httpClient = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        val jsonBody = jsonConverter.toJson(OtpConfirmationRequestBody(
            otpId = this.otpId,
            code = this.code
        ))

        val response = httpClient.post("api/v1/users/confirm-sign-up-with-otp", jsonBody)

        return if (response.isSuccessful) {
            Response.success(null)
        } else {
            if (response.code in 400..499) {
                val responseBody = jsonConverter
                    .fromJson(response.body!!.source!!, ErrorResponseBody::class)

                val mappedErrors = responseBody.errors.map { error ->
                    when {
                        error.code == "otpIdNotFound" -> SignUpOtpConfirmationError.OTP_NOT_FOUND
                        error.code == "incorrectOtp" -> SignUpOtpConfirmationError.INCORRECT_CODE
                        error.code == "expiredOtp" -> SignUpOtpConfirmationError.EXPIRED_CODE
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