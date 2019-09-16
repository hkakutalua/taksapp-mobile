package com.taksapp.taksapp.data.webservices.client.resources.common.errors

enum class LoginRequestError(val errorCode: String) {
    ACCOUNT_DOES_NOT_EXISTS("account_does_not_exists"),
    INVALID_CREDENTIALS("invalid_credentials");

    companion object {
        fun of(errorCode: String) : LoginRequestError {
            for (value in LoginRequestError.values()) {
                if (value.errorCode == errorCode)
                    return value
            }

            throw Exception("No enum constant with errorCode $errorCode")
        }
    }
}