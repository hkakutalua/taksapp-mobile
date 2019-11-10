package com.taksapp.taksapp.data.repositories

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.UserType
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.LoginRequestError
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.IOException

enum class LoginError {
    INCORRECT_CREDENTIALS,
    INEXISTENT_ACCOUNT,
    UNSUPPORTED_CLIENT,
    SERVER_ERROR,
    INTERNET_ERROR,
}

class AuthenticationRepository(private val taksapp: Taksapp) {
    fun loginAsRider(phoneNumber: String, password: String): LiveData<Result<Nothing, LoginError>> {
        return login(phoneNumber, password, UserType.Rider)
    }

    fun loginAsDriver(phoneNumber: String, password: String): LiveData<Result<Nothing, LoginError>> {
        return login(phoneNumber, password, UserType.Driver)
    }

    private fun login(
        phoneNumber: String,
        password: String,
        userType: UserType): LiveData<Result<Nothing, LoginError>> {

        val result = MutableLiveData<Result<Nothing, LoginError>>()
        result.value = Result.loading()

        val request = taksapp.users.loginRequestBuilder()
            .phoneNumber(phoneNumber)
            .password(password)
            .role(userType)
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
                        LoginRequestError.UNSUPPORTED_CLIENT -> result.postValue(Result.error(LoginError.UNSUPPORTED_CLIENT))
                    }
                }
            } catch (e: InternalServerErrorException) {
                result.postValue(Result.error(LoginError.SERVER_ERROR))
            } catch (e: IOException) {
                result.postValue(Result.error(LoginError.INTERNET_ERROR))
            }
        }

        return result
    }
}