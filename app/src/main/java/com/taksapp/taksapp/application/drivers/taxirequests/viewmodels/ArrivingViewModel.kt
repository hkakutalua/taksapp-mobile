package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestArrivalAnnounceError
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestCancellationError
import kotlinx.coroutines.launch
import java.io.IOException

class ArrivingViewModel(
    private val taxiRequest: TaxiRequestPresentationModel,
    private val driversTaxiRequestService: DriversTaxiRequestService,
    private val context: Context) : ViewModel() {

    private val _taxiRequestPresentation = MutableLiveData<TaxiRequestPresentationModel>()
    private val _navigateToArrivedEvent = MutableLiveData<Event<TaxiRequestPresentationModel>>()
    private val _processing = MutableLiveData<Boolean>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()
    private val _navigateToMain = MutableLiveData<Event<Nothing>>()
    private val _navigateToMainWithErrorEvent = MutableLiveData<Event<String>>()

    val taxiRequestPresentation: LiveData<TaxiRequestPresentationModel> = _taxiRequestPresentation
    val navigateToArrivedEvent: LiveData<Event<TaxiRequestPresentationModel>> = _navigateToArrivedEvent
    val processing: LiveData<Boolean> = _processing
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent
    val navigateToMain: LiveData<Event<Nothing>> = _navigateToMain
    val navigateToMainWithErrorEvent: LiveData<Event<String>> = _navigateToMainWithErrorEvent

    init {
        _taxiRequestPresentation.value = taxiRequest
    }

    fun announceArrival() {
        _processing.value = true

        viewModelScope.launch {
            try {
                val result = driversTaxiRequestService.announceArrival()

                if (result.isSuccessful) {
                    _navigateToArrivedEvent.postValue(Event(taxiRequest))

                } else if (result.hasFailed) {
                    when (result.error) {
                        TaxiRequestArrivalAnnounceError.SERVER_ERROR ->
                            _snackBarErrorEvent.postValue(
                                Event(context.getString(R.string.text_server_error)))

                        TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_FOUND ->
                            _navigateToMainWithErrorEvent.postValue(
                                Event(context.getString(R.string.text_taxi_request_already_cancelled)))

                        TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS ->
                            navigateWhenCurrentTaxiRequestStatusChanges()
                    }
                }
            } catch (e: IOException) {
              _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                _processing.postValue(false)
            }
        }
    }

    fun cancelTaxiRequest() {
        _processing.value = true

        viewModelScope.launch {
            try {
                val result = driversTaxiRequestService.cancelCurrentTaxiRequest()
                if (result.isSuccessful) {
                    _navigateToMain.postValue(Event(null))

                } else if (result.hasFailed) {
                    when (result.error) {
                        TaxiRequestCancellationError.TAXI_REQUEST_NOT_FOUND ->
                            _navigateToMainWithErrorEvent.postValue(
                                Event(context.getString(R.string.text_taxi_request_already_cancelled)))

                        TaxiRequestCancellationError.SERVER_ERROR ->
                            _snackBarErrorEvent.postValue(
                                Event(context.getString(R.string.text_server_error)))
                    }
                }

            } catch (e: IOException) {
              _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                _processing.postValue(false)
            }
        }
    }

    private suspend fun navigateWhenCurrentTaxiRequestStatusChanges() {
        val currentTaxiRequestResult = driversTaxiRequestService.getCurrentTaxiRequest()

        if (currentTaxiRequestResult.isSuccessful) {
            val taxiRequest = currentTaxiRequestResult.data

            if (taxiRequest?.id != this@ArrivingViewModel.taxiRequest.id) {
                _navigateToMain.postValue(Event(null))
                return
            }

            if (taxiRequest.status == Status.DRIVER_ARRIVED) {
                _navigateToArrivedEvent.postValue(Event(this.taxiRequest))
            }
        }
    }
}