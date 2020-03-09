package com.taksapp.taksapp.application.riders.taxirequests.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.application.shared.mappers.TaxiRequestMapper
import com.taksapp.taksapp.application.shared.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.data.repositories.CancelTaxiRequestError
import com.taksapp.taksapp.data.repositories.GetTaxiRequestError
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class TaxiRequestViewModel(
    private var taxiRequest: TaxiRequest,
    private val riderTaxiRequestsRepository: RiderTaxiRequestsRepository,
    private val taskScheduler: TaskScheduler,
    private val context: Context
) :
    ViewModel() {
    companion object {
        const val TAXI_REQUEST_TIMEOUT_TASK_ID = "TAXI_REQUEST_TIMEOUT_TASK_ID"
        const val TAXI_REQUEST_PULL_TASK_ID = "TAXI_REQUEST_PULL_TASK_ID"
    }

    private val _cancellingTaxiRequest = MutableLiveData<Boolean>()
    private val _showTimeoutMessageAndNavigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _showCancelledMessageAndNavigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateBackEvent = MutableLiveData<Event<Nothing>>()
    private val _navigateToAcceptedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _navigateToDriverArrivedStateEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()
    private val _centerMapOnDriverEvent = MutableLiveData<Event<LocationPresentationModel>>()
    private val _taxiRequestPresentation = MutableLiveData<TaxiRequestPresentationModel>()

    val cancellingTaxiRequest: LiveData<Boolean> = _cancellingTaxiRequest
    val showTimeoutMessageAndNavigateBackEvent: LiveData<Event<Nothing>> =
        _showTimeoutMessageAndNavigateBackEvent
    val showCancelledMessageAndNavigateBackEvent: LiveData<Event<Nothing>> =
        _showCancelledMessageAndNavigateBackEvent
    val navigateBackEvent: LiveData<Event<Nothing>> = _navigateBackEvent
    val navigateToAcceptedStateEvent: LiveData<Event<TaxiRequest>> = _navigateToAcceptedStateEvent
    val navigateToDriverArrivedStateEvent: LiveData<Event<TaxiRequest>> =
        _navigateToDriverArrivedStateEvent
    val centerMapOnDriverEvent: LiveData<Event<LocationPresentationModel>> = _centerMapOnDriverEvent
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent
    val taxiRequestPresentation: LiveData<TaxiRequestPresentationModel> = _taxiRequestPresentation

    init {
        EventBus.getDefault().register(this)

        if (taxiRequest.status == Status.WAITING_ACCEPTANCE) {
            taskScheduler.schedule(TAXI_REQUEST_TIMEOUT_TASK_ID, taxiRequest.expirationDate) {
                _showTimeoutMessageAndNavigateBackEvent.postValue(Event(null))
            }
        }

        taskScheduler.schedule(
            TAXI_REQUEST_PULL_TASK_ID,
            10.toDuration(TimeUnit.SECONDS)
        ) { syncTaxiRequestStatusChange(taxiRequest.id) }

        _taxiRequestPresentation.value = TaxiRequestMapper().map(taxiRequest)
        navigateToCorrectDestinationGivenStatus(taxiRequest.status)
    }

    override fun onCleared() {
        super.onCleared()
        taskScheduler.cancel(TAXI_REQUEST_TIMEOUT_TASK_ID)
        taskScheduler.cancel(TAXI_REQUEST_PULL_TASK_ID)
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {
        syncTaxiRequestStatusChange(event.taxiRequestId)
    }

    fun cancelCurrentTaxiRequest() {
        _cancellingTaxiRequest.value = true
        taskScheduler.pause(TAXI_REQUEST_TIMEOUT_TASK_ID)

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.updateCurrentAsCancelled()
                if (result.isSuccessful || hasNoTaxiRequestToBeCancelled(result)) {
                    _navigateBackEvent.postValue(Event(null))
                } else if (result.error == CancelTaxiRequestError.SERVER_ERROR) {
                    _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_server_error)))
                }

                _cancellingTaxiRequest.postValue(false)
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                taskScheduler.resume(TAXI_REQUEST_TIMEOUT_TASK_ID)
            }
        }
    }

    fun centerMapOnDriver() {
        taxiRequestPresentation.value?.driverLocation?.let { driverLocation ->
            _centerMapOnDriverEvent.value = Event(driverLocation)
        }
    }

    private fun syncTaxiRequestStatusChange(taxiRequestId: String) {
        taskScheduler.pause(TAXI_REQUEST_TIMEOUT_TASK_ID)

        viewModelScope.launch {
            try {
                val currentTaxiRequestResult = riderTaxiRequestsRepository.getCurrent()

                if (currentTaxiRequestResult.isSuccessful) {
                    val updatedTaxiRequest = currentTaxiRequestResult.data!!

                    if (canSynchronize(updatedTaxiRequest)) {
                        taxiRequest = updatedTaxiRequest
                        navigateToCorrectDestinationGivenStatus(taxiRequest.status)
                    }

                    _taxiRequestPresentation.value = TaxiRequestMapper().map(taxiRequest)
                    return@launch
                }

                if (hasNoCurrentTaxiRequest(currentTaxiRequestResult)) {
                    val taxiRequestResult = riderTaxiRequestsRepository.getById(taxiRequestId)
                    val updatedTaxiRequest = taxiRequestResult.data

                    if (taxiRequestResult.isSuccessful &&
                        updatedTaxiRequest?.status == Status.CANCELLED) {

                        if (updatedTaxiRequest.status == Status.CANCELLED) {
                            taxiRequest = updatedTaxiRequest
                            navigateToCorrectDestinationGivenStatus(taxiRequest.status)
                        }

                        _taxiRequestPresentation.value = TaxiRequestMapper().map(taxiRequest)
                    }
                }
            } catch (e: IOException) { }
            finally {
                taskScheduler.resume(TAXI_REQUEST_TIMEOUT_TASK_ID)
            }
        }
    }

    private fun canSynchronize(updatedTaxiRequest: TaxiRequest) =
        updatedTaxiRequest != taxiRequest ||
                updatedTaxiRequest.status != taxiRequest.status

    private fun navigateToCorrectDestinationGivenStatus(status: Status) {
        viewModelScope.launch(Dispatchers.Main) {
            when (status) {
                Status.ACCEPTED -> {
                    _navigateToAcceptedStateEvent.value = Event(taxiRequest)
                    taskScheduler.cancel(TAXI_REQUEST_TIMEOUT_TASK_ID)
                }
                Status.DRIVER_ARRIVED -> {
                    _navigateToDriverArrivedStateEvent.value = Event(taxiRequest)
                    taskScheduler.cancel(TAXI_REQUEST_TIMEOUT_TASK_ID)
                }
                Status.CANCELLED -> _showCancelledMessageAndNavigateBackEvent.value = Event(null)
            }
        }
    }

    private fun hasNoCurrentTaxiRequest(result: Result<TaxiRequest, GetTaxiRequestError>) =
        result.error == GetTaxiRequestError.NO_TAXI_REQUEST

    private fun hasNoTaxiRequestToBeCancelled(result: Result<Nothing, CancelTaxiRequestError>) =
        result.error == CancelTaxiRequestError.NO_TAXI_REQUEST
}