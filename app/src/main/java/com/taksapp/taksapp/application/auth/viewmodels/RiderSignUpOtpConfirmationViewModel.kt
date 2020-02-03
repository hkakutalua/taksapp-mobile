package com.taksapp.taksapp.application.auth.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.OtpConfirmationError

class RiderSignUpOtpConfirmationViewModel(
    private val authenticationRepository: AuthenticationRepository,
    private val context: Context) : ViewModel() {

    private val _otpError = MutableLiveData<String>()
    private val _loading = MutableLiveData<Boolean>()
    private val _snackBarMessage = MutableLiveData<Event<String>>()
    private val _navigateToRiderLogin = MutableLiveData<Event<Nothing>>()

    val otp = MutableLiveData<String>()
    val otpError: LiveData<String> = _otpError
    val loading: LiveData<Boolean> = _loading
    val snackBarMessage: LiveData<Event<String>> = _snackBarMessage
    val navigateToRiderLogin: LiveData<Event<Nothing>> = _navigateToRiderLogin

    init {
        otp.observeForever { _otpError.value = null }
        snackBarMessage.observeForever { _otpError.value = null }
    }

    fun confirmSignUpWithOtp(otpId: String) {
        if (otp.value.isNullOrBlank()) {
            _otpError.value = context.getString(R.string.error_type_the_sign_up_otp)
            return
        }

        if (otp.value!!.length != 6) {
            _otpError.value = context.getString(R.string.error_type_a_correct_otp)
            return
        }

        authenticationRepository.confirmSignUpWithOtp(otpId, otp.value!!)
            .observeForever { result ->
                _loading.value = result.isLoading

                if (result.isSuccessful) {
                    _navigateToRiderLogin.value = Event(result.data)
                } else if (result.hasFailed) {
                    val error: OtpConfirmationError? = result.error

                    if (error != null) {
                        when (error) {
                            OtpConfirmationError.INCORRECT_CODE ->
                                _otpError.value = context.getString(R.string.error_incorrect_otp)
                            OtpConfirmationError.EXPIRED_OTP ->
                                _otpError.value = context.getString(R.string.error_otp_expired)
                            OtpConfirmationError.INTERNAL_SERVER_ERROR ->
                                _snackBarMessage.value = Event(context.getString(R.string.text_server_error))
                            OtpConfirmationError.INTERNET_CONNECTION_ERROR ->
                                _snackBarMessage.value = Event(context.getString(R.string.text_internet_error))
                        }
                    }
                }
            }
    }
}