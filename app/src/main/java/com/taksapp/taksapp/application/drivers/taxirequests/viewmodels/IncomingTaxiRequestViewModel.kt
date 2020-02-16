package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.application.shared.mappers.TaxiRequestMapper
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
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
class IncomingTaxiRequestViewModel(
    private val taxiRequest: TaxiRequestPresentationModel,
    private val driversTaxiRequestService: DriversTaxiRequestService,
    private val taskScheduler: TaskScheduler,
    private val context: Context) : ViewModel() {

    companion object {
        const val TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID = "TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID"
        private const val TAXI_REQUEST_COUNTDOWN_SECONDS = 10L
    }

    private val _taxiRequestPresentation = MutableLiveData<TaxiRequestPresentationModel>()
    private val _startTaxiRequestSecondsCountdownEvent = MutableLiveData<Event<Long>>()
    private val _pauseTaxiRequestCountdownEvent = MutableLiveData<Event<Nothing>>()
    private val _resumeTaxiRequestCountdownEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToMainScreen = MutableLiveData<Event<Nothing>>()
    private val _navigateToTaxiRequestEvent = MutableLiveData<Event<TaxiRequestPresentationModel>>()
    private val _navigateToMainScreenWithErrorEvent = MutableLiveData<Event<String>>()
    private val _acceptingTaxiRequest = MutableLiveData<Boolean>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()

    val taxiRequestPresentation: LiveData<TaxiRequestPresentationModel> = _taxiRequestPresentation
    val startTaxiRequestSecondsCountdownEvent: LiveData<Event<Long>> =
        _startTaxiRequestSecondsCountdownEvent
    val pauseTaxiRequestCountdownEvent: LiveData<Event<Nothing>> = _pauseTaxiRequestCountdownEvent
    val resumeTaxiRequestCountdownEvent: LiveData<Event<Nothing>> = _resumeTaxiRequestCountdownEvent
    val navigateToMainScreen: LiveData<Event<Nothing>> =
        _navigateToMainScreen
    val navigateToTaxiRequestEvent: LiveData<Event<TaxiRequestPresentationModel>> =
        _navigateToTaxiRequestEvent
    val navigateToMainScreenWithErrorEvent: LiveData<Event<String>> =
        _navigateToMainScreenWithErrorEvent
    val acceptingTaxiRequest: LiveData<Boolean> =
        _acceptingTaxiRequest
    val snackBarErrorEvent: MutableLiveData<Event<String>> = _snackBarErrorEvent

    init {
        _taxiRequestPresentation.value = taxiRequest

        taskScheduler.schedule(
            TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID,
            DateTime.now().plusSeconds(TAXI_REQUEST_COUNTDOWN_SECONDS.toInt())
        ) { _navigateToMainScreen.value = Event(null) }

        _startTaxiRequestSecondsCountdownEvent.value = Event(TAXI_REQUEST_COUNTDOWN_SECONDS)
    }

    fun acceptTaxiRequest() {
        _acceptingTaxiRequest.value = true

        taskScheduler.pause(TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
        _pauseTaxiRequestCountdownEvent.value = Event(null)

        viewModelScope.launch {
            try {
                val taxiRequestAcceptanceResult =
                    driversTaxiRequestService.acceptTaxiRequest(taxiRequest.id)

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
                taskScheduler.resume(TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
                _resumeTaxiRequestCountdownEvent.postValue(Event(null))
            }
        }
    }

    private fun navigateToTaxiRequest(
        taxiRequestRetrievalResult: Result<TaxiRequest, TaxiRequestRetrievalError>) {

        if (taxiRequestRetrievalResult.isSuccessful) {
            val taxiRequest = taxiRequestRetrievalResult.data
            taxiRequest?.let {
                taskScheduler.cancel(TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
                val event = Event(TaxiRequestMapper().map(taxiRequest))
                _navigateToTaxiRequestEvent.postValue(event)
            }
        }
    }
}