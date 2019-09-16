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

enum class LoginError {
    INCORRECT_CREDENTIALS,
    INEXISTENT_ACCOUNT,
    SERVER_ERROR,
    INTERNET_ERROR,
}

abstract class AuthenticationRepository(private val pushNotificationTokenRetriever: PushNotificationTokenRetriever) {

    abstract fun getLoginRequestBuilder() : LoginRequest.BaseBuilder

    fun login(email: String, password: String): LiveData<Result<Nothing, LoginError>> {
        val result = MutableLiveData<Result<Nothing, LoginError>>()
        result.value = Result.loading()

        pushNotificationTokenRetriever.getPushNotificationToken { tokenResult ->
            if (tokenResult.isFailure) {
                result.value = Result.error(LoginError.INTERNET_ERROR)
                return@getPushNotificationToken
            }

            val request = getLoginRequestBuilder()
                .email(email)
                .password(password)
                .pushNotificationToken(tokenResult.getOrThrow())
                .build()

            GlobalScope.launch {
                try {
                    val response = request.login()
                    if (response.successful) {
                        result.postValue(Result.success(null))
                    } else {
                        when (response.errorCode) {
                            LoginRequestError.ACCOUNT_DOES_NOT_EXISTS -> result.postValue(Result.error(LoginError.INEXISTENT_ACCOUNT))
                            LoginRequestError.INVALID_CREDENTIALS -> result.postValue(Result.error(LoginError.INCORRECT_CREDENTIALS))
                        }
                    }
                } catch (e: InternalServerErrorException) {
                    result.postValue(Result.error(LoginError.SERVER_ERROR))
                } catch (e: IOException) {
                    result.postValue(Result.error(LoginError.INTERNET_ERROR))
                }
            }
        }

        return result
    }
}