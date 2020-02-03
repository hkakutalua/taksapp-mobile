package com.taksapp.taksapp.application.auth.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.SignUpFields

class RiderSignUpViewModel(
    private val repository: AuthenticationRepository,
    private val context: Context): ViewModel() {

    private val _loading = MutableLiveData<Boolean>()
    private val _firstAndLastNamesError = MutableLiveData<String>()
    private val _phoneNumberError = MutableLiveData<String>()
    private val _passwordError = MutableLiveData<String>()
    private val _navigateToOtpConfirmation = MutableLiveData<Event<String>>()
    private val _snackBarError = MutableLiveData<Event<String>>()

    val loading: LiveData<Boolean> = _loading
    val firstAndLastNamesError: LiveData<String> = _firstAndLastNamesError
    val phoneNumberError: LiveData<String> = _phoneNumberError
    val passwordError: LiveData<String> = _passwordError
    val firstAndLastNames = MutableLiveData<String>()
    val phoneNumber = MutableLiveData<String>()
    val password = MutableLiveData<String>()
    val navigateToOtpConfirmation: LiveData<Event<String>> = _navigateToOtpConfirmation
    val snackBarError: LiveData<Event<String>> = _snackBarError


    init {
        firstAndLastNames.observeForever { _firstAndLastNamesError.value = null }
        phoneNumber.observeForever { _phoneNumberError.value = null }
        password.observeForever { _passwordError.value = null }
    }

    fun signUp() {
        if (!validateFields())
            return

        repository.startSignUpAsRider(
            firstName = extractFirstName(firstAndLastNames.value),
            lastName = extractLastName(firstAndLastNames.value),
            phoneNumber = "+244${phoneNumber.value}",
            password = password.value.orEmpty()
        ).observeForever { result ->
            _loading.value = result.isLoading

            if (result.isSuccessful) {
                _navigateToOtpConfirmation.value = Event(result.data)
            } else if (result.hasFailed) {
                handleErrorsIfAvailable(result)
            }
        }
    }

    private fun handleErrorsIfAvailable(result: Result<String, Map<SignUpFields, String>>) {
        val errorsMap = result.error
        if (errorsMap != null) {
            _phoneNumberError.value = errorsMap[SignUpFields.PHONE_NUMBER]

            if (errorsMap[SignUpFields.NO_FIELD] != null) {
                _snackBarError.value = Event(errorsMap[SignUpFields.NO_FIELD])
            }
        }
    }

    private fun extractFirstName(fullName: String?): String {
        return fullName
            ?.trim()
            ?.split(" ")
            ?.first().orEmpty()
    }

    private fun extractLastName(fullName: String?): String {
        return fullName
            ?.trim()
            ?.split(" ")
            ?.last().orEmpty()
    }

    private fun validateFields(): Boolean {
        var validFields = true

        if (firstAndLastNames.value.isNullOrBlank()) {
            _firstAndLastNamesError.value =
                context.getString(R.string.error_type_your_first_and_last_names)
            validFields = false
        }

        if (!firstAndLastNames.value.isNullOrBlank() &&
            firstAndLastNames.value!!.split(" ").size != 2) {
            _firstAndLastNamesError.value =
                context.getString(R.string.error_type_yout_first_and_last_names_only)
            validFields = false
        }

        if (phoneNumber.value.isNullOrBlank()) {
            _phoneNumberError.value = context.getString(R.string.error_type_your_phone_number)
            validFields = false
        }

        if (!phoneNumber.value.isNullOrBlank() &&
            phoneNumber.value!!.length < 9) {
            _phoneNumberError.value = context.getString(R.string.error_type_a_correct_phone_number)
            validFields = false
        }

        if (password.value.isNullOrBlank()) {
            _passwordError.value = context.getString(R.string.error_type_your_password)
            validFields = false
        }

        return validFields
    }
}