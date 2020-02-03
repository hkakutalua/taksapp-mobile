package com.taksapp.taksapp.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.UserType
import com.taksapp.taksapp.data.webservices.client.exceptions.InternalServerErrorException
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.LoginRequestError
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.SignUpError
import com.taksapp.taksapp.data.webservices.client.resources.users.errors.SignUpOtpConfirmationError
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

enum class OtpConfirmationError {
    INCORRECT_CODE,
    EXPIRED_OTP,
    INTERNAL_SERVER_ERROR,
    INTERNET_CONNECTION_ERROR
}

enum class LoginStatus {
    LOGGED_IN_AS_RIDER,
    LOGGED_IN_AS_DRIVER,
    NOT_LOGGED_IN
}

class AuthenticationRepository(
    private val taksapp: Taksapp,
    private val context: Context) {
    fun loginAsRider(phoneNumber: String, password: String): LiveData<Result<Nothing, LoginError>> {
        return login(phoneNumber, password, UserType.RIDER)
    }

    fun loginAsDriver(phoneNumber: String, password: String): LiveData<Result<Nothing, LoginError>> {
        return login(phoneNumber, password, UserType.DRIVER)
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

    fun confirmSignUpWithOtp(otpId: String, code: String)
            : LiveData<Result<Nothing, OtpConfirmationError>> {

        val result = MutableLiveData<Result<Nothing, OtpConfirmationError>>()
        result.value = Result.loading()

        val signUpOtpConfirmationRequest = taksapp.users.signUpOtpConfirmationBuilder()
            .otpId(otpId)
            .code(code)
            .build()

        GlobalScope.launch {
            try {
                val otpConfirmationResponse = signUpOtpConfirmationRequest.confirmOtp()
                if (otpConfirmationResponse.successful) {
                    result.postValue(Result.success(null))
                } else {
                    val errors = otpConfirmationResponse.error?.map { error: SignUpOtpConfirmationError ->
                        when (error) {
                            SignUpOtpConfirmationError.OTP_NOT_FOUND -> OtpConfirmationError.EXPIRED_OTP
                            SignUpOtpConfirmationError.EXPIRED_CODE -> OtpConfirmationError.EXPIRED_OTP
                            SignUpOtpConfirmationError.INCORRECT_CODE -> OtpConfirmationError.INCORRECT_CODE
                        }
                    }

                    result.postValue(Result.error(errors?.firstOrNull()))
                }
            } catch (e: InternalServerErrorException) {
                result.postValue(Result.error(OtpConfirmationError.INTERNAL_SERVER_ERROR))
            } catch (e: IOException) {
                result.postValue(Result.error(OtpConfirmationError.INTERNET_CONNECTION_ERROR))
            }
        }

        return result
    }

    fun getLoginStatus(): LiveData<LoginStatus> {
        val sessionStore = taksapp.sessionStore
        val loginStatus = MutableLiveData<LoginStatus>()

        if (sessionStore.getAccessToken().isNotBlank()) {
            when (sessionStore.getUserType()) {
                UserType.RIDER -> loginStatus.value = LoginStatus.LOGGED_IN_AS_RIDER
                UserType.DRIVER -> loginStatus.value = LoginStatus.LOGGED_IN_AS_DRIVER
            }
        } else {
            loginStatus.value = LoginStatus.NOT_LOGGED_IN
        }

        return loginStatus
    }
}