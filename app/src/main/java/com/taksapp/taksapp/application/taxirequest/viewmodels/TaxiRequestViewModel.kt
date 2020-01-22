package com.taksapp.taksapp.application.taxirequest.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import kotlinx.coroutines.launch
import java.io.IOException
import java.util.*
import kotlin.concurrent.schedule

class TaxiRequestViewModel(
    private val taxiRequest: TaxiRequest,
    private val riderTaxiRequestsRepository: RiderTaxiRequestsRepository,
    private val context: Context
) :
    ViewModel() {
    private val _cancellingTaxiRequest = MutableLiveData<Boolean>()
    private val _showTimeoutMessageAndNavigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToAcceptedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _navigateToDriverArrivedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()

    val cancellingTaxiRequest: LiveData<Boolean> = _cancellingTaxiRequest
    val showTimeoutMessageAndNavigateBackEvent: LiveData<Event<Nothing>> = _showTimeoutMessageAndNavigateBackEvent
    val navigateToAcceptedStateEvent: LiveData<Event<TaxiRequest>> = _navigateToAcceptedStateEvent
    val navigateToDriverArrivedStateEvent: LiveData<Event<TaxiRequest>> = _navigateToDriverArrivedStateEvent
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent

    init {
        if (taxiRequest.status == Status.WAITING_ACCEPTANCE) {
            if (!taxiRequest.hasExpired()) {
                Timer().schedule(taxiRequest.expirationDate.toDate()) {
                    _showTimeoutMessageAndNavigateBackEvent.postValue(Event(null))
                }
            }
        } else if (taxiRequest.status == Status.ACCEPTED) {
            _navigateToAcceptedStateEvent.value = Event(taxiRequest)
        } else if (taxiRequest.status == Status.DRIVER_ARRIVED) {
            _navigateToDriverArrivedStateEvent.value = Event(taxiRequest)
        }
    }

    fun cancelCurrentTaxiRequest() {
        _cancellingTaxiRequest.value = true

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.updateCurrentAsCancelled()
                if (result.isSuccessful)
                    _showTimeoutMessageAndNavigateBackEvent.postValue(Event(null))

                _cancellingTaxiRequest.postValue(false)
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            }
        }
    }
}