package com.taksapp.taksapp.application.launch.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.data.repositories.AuthenticationRepository
import com.taksapp.taksapp.data.repositories.LoginStatus

class LaunchViewModel(
    private val authenticationRepository: AuthenticationRepository) : ViewModel() {
    private val _navigateToRiderMain = MutableLiveData<Event<Nothing>>()
    private val _navigateToDriverMain = MutableLiveData<Event<Nothing>>()
    private val _navigateToWelcome = MutableLiveData<Event<Nothing>>()

    val navigateToRiderMain: LiveData<Event<Nothing>> = _navigateToRiderMain
    val navigateToDriverMain: LiveData<Event<Nothing>> = _navigateToDriverMain
    val navigateToWelcome: LiveData<Event<Nothing>> = _navigateToWelcome

    fun evaluateIfLoggedIn() {
        authenticationRepository.getLoginStatus()
            .observeForever {
                when (it) {
                    LoginStatus.LOGGED_IN_AS_RIDER -> _navigateToRiderMain.value = Event(null)
                    LoginStatus.LOGGED_IN_AS_DRIVER -> _navigateToDriverMain.value = Event(null)
                    LoginStatus.NOT_LOGGED_IN -> _navigateToWelcome.value = Event(null)
                }
            }
    }
}