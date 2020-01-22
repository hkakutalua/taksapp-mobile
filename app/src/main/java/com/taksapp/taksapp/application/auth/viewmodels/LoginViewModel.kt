package com.taksapp.taksapp.application.auth.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.LoginError

class LoginViewModel(
    private val repository: AuthenticationRepository,
    private val context: Context) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _phoneNumberError = MutableLiveData<String>()
    private val _passwordError = MutableLiveData<String>()
    private val _snackBarError = MutableLiveData<String>()
    private val _navigateToMain = MutableLiveData<Event<Nothing>>()

    val loading : LiveData<Boolean> = _loading
    val phoneNumber = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val phoneNumberError : LiveData<String> = _phoneNumberError
    val passwordError : LiveData<String> = _passwordError
    val snackBarError : LiveData<String> = _snackBarError
    val navigateToMain : LiveData<Event<Nothing>> = _navigateToMain

    init {
        phoneNumber.observeForever { _phoneNumberError.value = null }
        password.observeForever { _passwordError.value = null }
    }

    fun loginAsRider() {
        if (!validateFields())
            return

        repository.loginAsRider("+244${phoneNumber.value!!}", password.value!!)
            .observeForever(getLoginObserver())
    }

    fun loginAsDriver() {
        if (!validateFields())
            return

        repository.loginAsDriver("+244${phoneNumber.value!!}", password.value!!)
            .observeForever(getLoginObserver())
    }

    private fun getLoginObserver(): Observer<Result<Nothing, LoginError>> {
        return Observer { result ->
            _loading.value = result.isLoading

            if (result.isSuccessful) {
                _navigateToMain.value = Event(null)
            } else if (result.hasFailed) {
                when (result.error) {
                    LoginError.INCORRECT_CREDENTIALS ->
                        _passwordError.value = context.getString(R.string.text_incorrect_password)
                    LoginError.INEXISTENT_ACCOUNT ->
                        _phoneNumberError.value = context.getString(R.string.text_inexistent_account)
                    LoginError.SERVER_ERROR ->
                        _snackBarError.value = context.getString(R.string.text_server_error)
                    LoginError.INTERNET_ERROR ->
                        _snackBarError.value = context.getString(R.string.text_internet_error)
                    LoginError.UNSUPPORTED_CLIENT ->
                        _snackBarError.value = context.getString(R.string.text_unsupported_application)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var successfulValidation = true

        if (phoneNumber.value.isNullOrBlank()) {
            _phoneNumberError.value = context.getString(R.string.error_type_your_phone_number)
            successfulValidation = false
        }

        if (!phoneNumber.value.isNullOrBlank() &&
            phoneNumber.value!!.length < 9) {
            _phoneNumberError.value = context.getString(R.string.error_type_a_correct_phone_number)
            successfulValidation = false
        }

        if (password.value.isNullOrBlank()) {
            _passwordError.value = context.getString(R.string.error_type_your_password)
            successfulValidation = false
        }

        return successfulValidation
    }
}