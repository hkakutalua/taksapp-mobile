package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.shared.mappers.TripMapper
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.application.shared.presentationmodels.TripPresentationModel
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.TripStatus
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.domain.interfaces.DriversTripsService
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class ArrivedViewModel(
    private val taxiRequestPresentationModel: TaxiRequestPresentationModel,
    private val driversTaxiRequestService: DriversTaxiRequestService,
    private val driversTripsService: DriversTripsService,
    taskScheduler: TaskScheduler,
    private val context: Context) : ViewModel() {
    companion object {
        const val TAXI_REQUEST_PULL_TASK_ID = "TAXI_REQUEST_PULL_TASK"
        const val TRIP_PULL_TASK_ID = "TRIP_PULL_TASK"
    }

    private val _taxiRequestPresentation = MutableLiveData<TaxiRequestPresentationModel>()
    private val _processing = MutableLiveData<Boolean>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()
    private val _navigateToMain = MutableLiveData<Event<Nothing>>()
    private val _navigateToMainWithErrorEvent = MutableLiveData<Event<String>>()
    private val _navigateToTripEvent = MutableLiveData<Event<TripPresentationModel>>()

    val taxiRequestPresentation: LiveData<TaxiRequestPresentationModel> = _taxiRequestPresentation
    val processing: LiveData<Boolean> = _processing
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarErrorEvent
    val navigateToMain: LiveData<Event<Nothing>> = _navigateToMain
    val navigateToMainWithErrorEvent: LiveData<Event<String>> = _navigateToMainWithErrorEvent
    val navigateToTripEvent: LiveData<Event<TripPresentationModel>> = _navigateToTripEvent

    init {
        _taxiRequestPresentation.value = taxiRequestPresentationModel

        EventBus.getDefault().register(this)

        taskScheduler.schedule(
            TAXI_REQUEST_PULL_TASK_ID,
            10.toDuration(TimeUnit.SECONDS)
        ) { navigateIfCurrentTaxiRequestStatusChanges() }

        taskScheduler.schedule(
            ArrivingViewModel.TRIP_PULL_TASK_ID,
            10.toDuration(TimeUnit.SECONDS)
        ) { navigateIfCurrentTripStatusChanges() }
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {
        navigateIfCurrentTaxiRequestStatusChanges()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTripStatusChanged(event: TripStatusChangedEvent) {
        navigateIfCurrentTripStatusChanges()
    }

    private fun navigateIfCurrentTripStatusChanges() {
        viewModelScope.launch {
            val tripResult = driversTripsService.getCurrentTrip()
            val trip = tripResult.data

            if (trip?.status != TripStatus.STARTED)
                return@launch

            trip.apply {
                val tripPresentation: TripPresentationModel = TripMapper().map(trip)
                _navigateToTripEvent.postValue(Event(tripPresentation))
            }
        }
    }

    private fun navigateIfCurrentTaxiRequestStatusChanges() {
        viewModelScope.launch {

            fun navigate(taxiRequest: TaxiRequest?) {
                when {
                    taxiRequest?.id != this@ArrivedViewModel.taxiRequestPresentationModel.id ->
                        _navigateToMain.postValue(Event(null))

                    taxiRequest.status == Status.CANCELLED ->
                        _navigateToMainWithErrorEvent.postValue(
                            Event(context.getString(R.string.text_taxi_request_already_cancelled)))
                }
            }

            val currentTaxiRequestResult = driversTaxiRequestService.getCurrentTaxiRequest()

            if (currentTaxiRequestResult.isSuccessful) {
                val taxiRequest = currentTaxiRequestResult.data
                navigate(taxiRequest)
            }

            if (currentTaxiRequestResult.error == TaxiRequestRetrievalError.NOT_FOUND) {
                val taxiRequestResult = driversTaxiRequestService
                    .getTaxiRequestById(taxiRequestPresentationModel.id)

                val taxiRequest = taxiRequestResult.data
                navigate(taxiRequest)
            }
        }
    }
}