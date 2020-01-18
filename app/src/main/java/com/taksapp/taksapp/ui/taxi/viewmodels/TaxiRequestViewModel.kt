package com.taksapp.taksapp.ui.taxi.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Event
import com.taksapp.taksapp.data.repositories.CreateTaxiRequestError
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.ui.taxi.presentationmodels.LocationPresentationModel
import kotlinx.coroutines.launch
import java.io.IOException

class TaxiRequestViewModel(
    private val riderTaxiRequestsRepository: RiderTaxiRequestsRepository,
    private val context: Context
) :
    ViewModel() {
    private val _sendingTaxiRequest = MutableLiveData<Boolean>()
    private val _cancellingTaxiRequest = MutableLiveData<Boolean>()
    private val _navigateToAcceptanceWaitEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToAcceptedEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToDriverArrivedEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToMainEvent = MutableLiveData<Event<Nothing>>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()

    val sendingTaxiRequest: LiveData<Boolean> = _sendingTaxiRequest
    val cancellingTaxiRequest: LiveData<Boolean> = _cancellingTaxiRequest
    val navigateToAcceptanceWaitEvent: LiveData<Event<Nothing>> = _navigateToAcceptanceWaitEvent
    val navigateToAcceptedEvent: LiveData<Event<Nothing>> = _navigateToAcceptedEvent
    val navigateToDriverArrivedEvent: LiveData<Event<Nothing>> = _navigateToDriverArrivedEvent
    val navigateToMainEvent: LiveData<Event<Nothing>> = _navigateToMainEvent
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent

    fun sendTaxiRequest(origin: LocationPresentationModel, destination: LocationPresentationModel) {
        _sendingTaxiRequest.value = true

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.create(
                    Location(origin.latitude, origin.longitude),
                    Location(destination.latitude, destination.longitude)
                )

                if (result.isSuccessful) {
                    val taxiRequest = result.data
                    when (taxiRequest?.status) {
                        Status.WAITING_ACCEPTANCE -> _navigateToAcceptanceWaitEvent.postValue(Event(null))
                        Status.ACCEPTED -> _navigateToAcceptedEvent.postValue(Event(null))
                        Status.DRIVER_ARRIVED -> _navigateToDriverArrivedEvent.postValue(Event(null))
                        else -> throw Exception("Unexpected state of taxi request: ${taxiRequest?.status}")
                    }
                } else if (result.hasFailed) {
                    var errorEvent = Event("")
                    when (result.error) {
                        CreateTaxiRequestError.NO_AVAILABLE_DRIVERS ->
                            errorEvent = Event(context.getString(R.string.error_no_available_drivers))
                        CreateTaxiRequestError.SERVER_ERROR ->
                            errorEvent = Event(context.getString(R.string.text_server_error))
                    }
                    _snackBarErrorEvent.postValue(errorEvent)
                }

                _sendingTaxiRequest.postValue(false)
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            }

            _sendingTaxiRequest.value = false
        }
    }

    fun cancelCurrentTaxiRequest() {
        _cancellingTaxiRequest.value = true

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.updateCurrentAsCancelled()
                if (result.isSuccessful)
                    _navigateToMainEvent.postValue(Event(null))

                _cancellingTaxiRequest.postValue(false)
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            }
        }

    }
}