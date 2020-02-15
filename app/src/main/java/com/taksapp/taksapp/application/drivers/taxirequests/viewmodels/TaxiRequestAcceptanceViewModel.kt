package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels;

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestAcceptanceError.*
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import kotlinx.coroutines.launch
import org.joda.time.DateTime

import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalTime
class TaxiRequestAcceptanceViewModel(
    private val driversTaxiRequestService: DriversTaxiRequestService,
    private val taskScheduler: TaskScheduler,
    private val context: Context) : ViewModel() {

    companion object {
        const val TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID = "TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID"

        private const val TAXI_REQUEST_COUNTDOWN_SECONDS = 10
    }

    private val _navigateToMainScreen = MutableLiveData<Event<Nothing>>()
    private val _navigateToTaxiRequestEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _navigateToMainScreenWithErrorEvent = MutableLiveData<Event<String>>()
    private val _acceptingTaxiRequest = MutableLiveData<Boolean>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()

    val navigateToMainScreen: LiveData<Event<Nothing>> =
        _navigateToMainScreen
    val navigateToTaxiRequestEvent: LiveData<Event<TaxiRequest>> =
        _navigateToTaxiRequestEvent
    val navigateToMainScreenWithErrorEvent: LiveData<Event<String>> =
        _navigateToMainScreenWithErrorEvent
    val acceptingTaxiRequest: LiveData<Boolean> =
        _acceptingTaxiRequest
    val snackBarErrorEvent: MutableLiveData<Event<String>> = _snackBarErrorEvent

    init {
        taskScheduler.schedule(
            TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID,
            DateTime.now().plusSeconds(TAXI_REQUEST_COUNTDOWN_SECONDS)
        ) { _navigateToMainScreen.value = Event(null) }
    }

    fun acceptTaxiRequest(taxiRequestId: String) {
        _acceptingTaxiRequest.value = true

        viewModelScope.launch {
            try {
                val taxiRequestAcceptanceResult =
                    driversTaxiRequestService.acceptTaxiRequest(taxiRequestId)

                if (taxiRequestAcceptanceResult.isSuccessful) {
                    val taxiRequestRetrievalResult =
                        driversTaxiRequestService.getCurrentTaxiRequest()
                    navigateToTaxiRequest(taxiRequestRetrievalResult)
                    return@launch
                }

                if (taxiRequestAcceptanceResult.hasFailed) {
                    if (taxiRequestAcceptanceResult.error == TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU) {
                        val taxiRequestRetrievalResult =
                            driversTaxiRequestService.getCurrentTaxiRequest()
                        navigateToTaxiRequest(taxiRequestRetrievalResult)
                    }

                    val errorMessage = when (taxiRequestAcceptanceResult.error) {
                        TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER ->
                            context.getString(R.string.error_taxi_request_accepted_by_another_driver)
                        TAXI_REQUEST_EXPIRED ->
                            context.getString(R.string.error_taxi_request_acceptance_failed_due_to_expiry)
                        else ->
                            context.getString(R.string.text_server_error)
                    }

                    _navigateToMainScreenWithErrorEvent.postValue(Event(errorMessage))
                }
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(
                    Event(context.getString(R.string.text_internet_error))
                )
            } finally {
                _acceptingTaxiRequest.postValue(false)
            }
        }
    }

    private fun navigateToTaxiRequest(
        taxiRequestRetrievalResult: Result<TaxiRequest, TaxiRequestRetrievalError>) {

        if (taxiRequestRetrievalResult.isSuccessful) {
            val taxiRequest = taxiRequestRetrievalResult.data
            taskScheduler.cancel(TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
            _navigateToTaxiRequestEvent.postValue(Event(taxiRequest))
        }
    }
}