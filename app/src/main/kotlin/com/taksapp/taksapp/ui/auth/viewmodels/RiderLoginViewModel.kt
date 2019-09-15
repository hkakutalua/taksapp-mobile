package com.taksapp.taksapp.ui.auth.viewmodels

import android.content.Context
import androidx.core.util.PatternsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Event
import com.taksapp.taksapp.data.repositories.LoginError
import com.taksapp.taksapp.data.repositories.RiderAuthenticationRepository

class RiderLoginViewModel(
    private val repository: RiderAuthenticationRepository,
    private val context: Context) : ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _emailError = MutableLiveData<String>()
    private val _passwordError = MutableLiveData<String>()
    private val _snackBarError = MutableLiveData<String>()
    private val _navigateToMain = MutableLiveData<Event<Nothing>>()

    val loading : LiveData<Boolean> = _loading
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val emailError : LiveData<String> = _emailError
    val passwordError : LiveData<String> = _passwordError
    val snackBarError : LiveData<String> = _snackBarError
    val navigateToMain : LiveData<Event<Nothing>> = _navigateToMain

    init {
        email.observeForever { _emailError.value = null }
        password.observeForever { _passwordError.value = null }
    }

    fun login() {
        if (!validateFields())
            return

        repository.login(email.value!!, password.value!!).observeForever { result ->
            _loading.value = result.isLoading

            if (result.isSuccessful) {
                _navigateToMain.value = Event(null)
            } else if (result.hasFailed) {
                when (result.error) {
                    LoginError.INCORRECT_CREDENTIALS ->
                        _passwordError.value = context.getString(R.string.text_incorrect_password)
                    LoginError.INEXISTENT_ACCOUNT ->
                        _emailError.value = context.getString(R.string.text_inexistent_account)
                    LoginError.SERVER_ERROR ->
                        _snackBarError.value = context.getString(R.string.text_server_error)
                    LoginError.INTERNET_ERROR ->
                        _snackBarError.value = context.getString(R.string.text_internet_error)
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        var successfulValidation = true

        if (email.value.isNullOrBlank()) {
            _emailError.value = context.getString(R.string.error_type_your_email)
            successfulValidation = false
        }

        if (email.value != null && !PatternsCompat.EMAIL_ADDRESS.matcher(email.value).matches()) {
            _emailError.value = context.getString(R.string.error_type_a_correct_email)
            successfulValidation = false
        }

        if (password.value.isNullOrBlank()) {
            _passwordError.value = context.getString(R.string.error_type_your_password)
            successfulValidation = false
        }

        return successfulValidation
    }
}