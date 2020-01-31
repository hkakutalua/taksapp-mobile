package com.taksapp.taksapp.application.taxirequest.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.taxirequest.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.application.taxirequest.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.data.repositories.CancelTaxiRequestError
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException

class TaxiRequestViewModel(
    taxiRequest: TaxiRequest?,
    private val riderTaxiRequestsRepository: RiderTaxiRequestsRepository,
    private val taskScheduler: TaskScheduler,
    private val context: Context
) :
    ViewModel() {
    private val _cancellingTaxiRequest = MutableLiveData<Boolean>()
    private val _showTimeoutMessageAndNavigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _showCancelledMessageAndNavigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToAcceptedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _navigateToDriverArrivedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()
    private val _taxiRequestPresentation = MutableLiveData<TaxiRequestPresentationModel>()

    private var taxiRequestExpiryTaskId: String? = null

    val cancellingTaxiRequest: LiveData<Boolean> = _cancellingTaxiRequest
    val showTimeoutMessageAndNavigateBackEvent: LiveData<Event<Nothing>> =
        _showTimeoutMessageAndNavigateBackEvent
    val showCancelledMessageAndNavigateBackEvent: LiveData<Event<Nothing>> =
        _showCancelledMessageAndNavigateBackEvent
    val navigateBackEvent: LiveData<Event<Nothing>> = _navigateBackEvent
    val navigateToAcceptedStateEvent: LiveData<Event<TaxiRequest>> = _navigateToAcceptedStateEvent
    val navigateToDriverArrivedStateEvent: LiveData<Event<TaxiRequest>> =
        _navigateToDriverArrivedStateEvent
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent
    val taxiRequestPresentation: LiveData<TaxiRequestPresentationModel> = _taxiRequestPresentation

    init {
        EventBus.getDefault().register(this)

        if (taxiRequest != null) {
            _taxiRequestPresentation.value = mapToTaxiRequestPresentationModel(taxiRequest)
        }

        if (taxiRequest?.status == Status.WAITING_ACCEPTANCE) {
            if (!taxiRequest.hasExpired()) {
                taxiRequestExpiryTaskId = taskScheduler.schedule(taxiRequest.expirationDate) {
                    _showTimeoutMessageAndNavigateBackEvent.postValue(Event(null))
                }
            }
        } else if (taxiRequest?.status == Status.ACCEPTED) {
            _navigateToAcceptedStateEvent.value = Event(taxiRequest)
        } else if (taxiRequest?.status == Status.DRIVER_ARRIVED) {
            _navigateToDriverArrivedStateEvent.value = Event(taxiRequest)
        }
    }

    override fun onCleared() {
        super.onCleared()
        taxiRequestExpiryTaskId?.let { taskScheduler.cancel(it) }
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {
        val taxiRequestStatus = event.taxiRequest.status

        if (taxiRequestStatus == Status.ACCEPTED) {
            taxiRequestExpiryTaskId?.let { taskScheduler.cancel(it) }
            _navigateToAcceptedStateEvent.value = Event(event.taxiRequest)
        } else if (taxiRequestStatus == Status.DRIVER_ARRIVED) {
            _navigateToDriverArrivedStateEvent.value = Event(event.taxiRequest)
        } else if (taxiRequestStatus == Status.CANCELLED) {
            _showCancelledMessageAndNavigateBackEvent.value = Event(null)
        }

        _taxiRequestPresentation.value = mapToTaxiRequestPresentationModel(event.taxiRequest)
    }

    fun cancelCurrentTaxiRequest() {
        _cancellingTaxiRequest.value = true
        taxiRequestExpiryTaskId?.let { taskScheduler.pause(it) }

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.updateCurrentAsCancelled()
                if (result.isSuccessful ||
                    result.error == CancelTaxiRequestError.NO_TAXI_REQUEST) {
                    _navigateBackEvent.postValue(Event(null))
                } else if (result.error == CancelTaxiRequestError.SERVER_ERROR) {
                    _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_server_error)))
                }

                _cancellingTaxiRequest.postValue(false)
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                taxiRequestExpiryTaskId?.let { taskScheduler.resume(it) }
            }
        }
    }

    private fun mapToTaxiRequestPresentationModel(taxiRequest: TaxiRequest) =
        TaxiRequestPresentationModel(
            origin = LocationPresentationModel(taxiRequest.origin.latitude, taxiRequest.origin.longitude),
            destination = LocationPresentationModel(taxiRequest.destination.latitude, taxiRequest.destination.longitude),
            originName = taxiRequest.originName,
            destinationName = taxiRequest.destinationName,
            driverName = "${taxiRequest.driver?.firstName} ${taxiRequest.driver?.lastName}"
        )
}