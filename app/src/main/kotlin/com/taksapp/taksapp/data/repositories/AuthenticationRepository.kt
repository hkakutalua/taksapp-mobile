package com.taksapp.taksapp.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.UserType
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.LoginRequestError
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.SignUpError
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

enum class SignUpFields {
    NO_FIELD,
    PHONE_NUMBER,
}

enum class OtpConfirmationFields {
    OTP,
}

class AuthenticationRepository(
    private val taksapp: Taksapp,
    private val context: Context) {
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
                    when (response.error) {
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

    fun startSignUpAsRider(
        firstName: String,
        lastName: String,
        phoneNumber: String,
        password: String): LiveData<Result<String, Map<SignUpFields, String>>> {

        val result = MutableLiveData<Result<String, Map<SignUpFields, String>>>()
        result.value = Result.loading()

        val signUpRequest = taksapp.users.signUpRequestBuilder()
            .firstName(firstName)
            .lastName(lastName)
            .phoneNumber(phoneNumber)
            .password(password)
            .build()

        GlobalScope.launch {
            try {
                val signUpResponse = signUpRequest.signUp()
                if (signUpResponse.successful) {
                    result.postValue(Result.success(signUpResponse.data))
                } else {
                    val errors = signUpResponse.error?.map { error: SignUpError ->
                        when (error) {
                            SignUpError.PHONE_NUMBER_ALREADY_REGISTERED ->
                                SignUpFields.PHONE_NUMBER to context.getString(R.string.error_phone_number_already_registered)
                        }
                    }

                    result.postValue(Result.error(errors?.toMap()))
                }
            } catch (e: InternalServerErrorException) {
                val errorsMap = mapOf(
                    SignUpFields.NO_FIELD to context.getString(R.string.text_server_error))
                result.postValue(Result.error(errorsMap))
            } catch (e: IOException) {
                val errorsMap = mapOf(
                    SignUpFields.NO_FIELD to context.getString(R.string.text_internet_error))
                result.postValue(Result.error(errorsMap))
            }
        }

        return result
    }
}