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
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
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
class ArrivingViewModel(
    private val taxiRequestPresentationModel: TaxiRequestPresentationModel,
    private val driversTaxiRequestService: DriversTaxiRequestService,
    taskScheduler: TaskScheduler,
    private val context: Context) : ViewModel() {
    companion object {
        const val TAXI_REQUEST_PULL_TASK_ID = "TAXI_REQUEST_PULL_TASK"
    }

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
        _taxiRequestPresentation.value = taxiRequestPresentationModel

        EventBus.getDefault().register(this)

        taskScheduler.schedule(
            TAXI_REQUEST_PULL_TASK_ID,
            10.toDuration(TimeUnit.SECONDS)
        ) { navigateIfCurrentTaxiRequestStatusChanges() }
    }

    fun announceArrival() {
        _processing.value = true

        viewModelScope.launch {
            try {
                val result = driversTaxiRequestService.announceArrival()

                if (result.isSuccessful) {
                    _navigateToArrivedEvent.postValue(Event(taxiRequestPresentationModel))

                } else if (result.hasFailed) {
                    when (result.error) {
                        TaxiRequestArrivalAnnounceError.SERVER_ERROR ->
                            _snackBarErrorEvent.postValue(
                                Event(context.getString(R.string.text_server_error)))

                        TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_FOUND ->
                            _navigateToMainWithErrorEvent.postValue(
                                Event(context.getString(R.string.text_taxi_request_already_cancelled)))

                        TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS ->
                            navigateIfCurrentTaxiRequestStatusChanges()
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {
        navigateIfCurrentTaxiRequestStatusChanges()
    }

    private fun navigateIfCurrentTaxiRequestStatusChanges() {
        viewModelScope.launch {

            fun navigate(taxiRequest: TaxiRequest?) {
                when {
                    taxiRequest?.id != this@ArrivingViewModel.taxiRequestPresentationModel.id ->
                        _navigateToMain.postValue(Event(null))

                    taxiRequest.status == Status.DRIVER_ARRIVED ->
                        _navigateToArrivedEvent.postValue(
                            Event(this@ArrivingViewModel.taxiRequestPresentationModel))

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