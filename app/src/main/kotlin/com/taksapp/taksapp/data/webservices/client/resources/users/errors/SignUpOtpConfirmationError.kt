package com.taksapp.taksapp.data.webservices.client.resources.users.errors

enum class SignUpOtpConfirmationError {
    OTP_NOT_FOUND,

    INCORRECT_CODE,

    EXPIRED_CODE
}