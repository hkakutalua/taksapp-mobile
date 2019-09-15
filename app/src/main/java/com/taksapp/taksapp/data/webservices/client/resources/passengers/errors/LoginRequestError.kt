package com.taksapp.taksapp.data.webservices.client.resources.passengers.errors

enum class LoginRequestError(val errorCode: String){
    ACCOUNT_DOES_NOT_EXISTS("account_does_not_exists"),
    INVALID_CREDENTIALS("invalid_credentials")
}