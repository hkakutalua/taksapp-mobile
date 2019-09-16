package com.taksapp.taksapp.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.PushNotificationTokenRetriever
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.errors.LoginRequestError
import com.taksapp.taksapp.data.webservices.client.resources.common.requests.LoginRequest
import java.io.IOException

class DriverAuthenticationRepository(
    private val taksapp: Taksapp,
    pushNotificationTokenRetriever: PushNotificationTokenRetriever)
    : AuthenticationRepository(pushNotificationTokenRetriever) {

    override fun getLoginRequestBuilder() = taksapp.drivers.loginRequestBuilder()
}