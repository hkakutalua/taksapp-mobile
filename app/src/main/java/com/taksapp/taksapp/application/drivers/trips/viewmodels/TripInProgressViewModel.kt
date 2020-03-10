package com.taksapp.taksapp.application.drivers.trips.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.application.shared.mappers.TripMapper
import com.taksapp.taksapp.application.shared.presentationmodels.TripPresentationModel
import com.taksapp.taksapp.domain.TripStatus
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTripsService
import com.taksapp.taksapp.domain.interfaces.DriversTripsService.TripFinishError
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
class TripInProgressViewModel(
    private val tripPresentation: TripPresentationModel,
    private val driversTripsService: DriversTripsService,
    private val taskScheduler: TaskScheduler,
    private val context: Context
) : ViewModel() {
    companion object {
        const val TRIP_PULL_TASK_ID = "TRIP_PULL_TASK"
    }

    private val _tripPresentation = MutableLiveData<TripPresentationModel>()
    private val _processing = MutableLiveData<Boolean>()
    private val _navigateToFinished = MutableLiveData<Event<TripPresentationModel>>()
    private val _snackBarError = MutableLiveData<Event<String>>()

    val processing: LiveData<Boolean> = _processing
    val navigateToFinished: LiveData<Event<TripPresentationModel>> = _navigateToFinished
    val snackBarErrorEvent: LiveData<Event<String>> = _snackBarError

    init {
        _tripPresentation.value = tripPresentation
        EventBus.getDefault().register(this)

        taskScheduler.schedule(TRIP_PULL_TASK_ID, 15.toDuration(TimeUnit.SECONDS))
            { navigateIfCurrentTripStatusChanges() }
    }

    fun finishTrip() {
        _processing.value = true

        viewModelScope.launch {
            try {
                val response = driversTripsService.finishCurrentTrip()
                if (response.isSuccessful) {
                    _navigateToFinished.postValue(Event(tripPresentation))
                } else if (response.hasFailed) {
                    when (response.error) {
                        TripFinishError.TRIP_NOT_FOUND ->
                            _navigateToFinished.postValue(Event(tripPresentation))
                        TripFinishError.SERVER_ERROR ->
                            _snackBarError.postValue(Event(context.getString(R.string.text_server_error)))
                        else ->
                            _snackBarError.postValue(Event(context.getString(R.string.text_server_error)))
                    }
                }
            } catch(e: IOException) {
              _snackBarError.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                _processing.postValue(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTripStatusChanged(event: TripStatusChangedEvent) {
        navigateIfCurrentTripStatusChanges()
    }

    private fun navigateIfCurrentTripStatusChanges() {
        viewModelScope.launch {
            val tripResult = driversTripsService.getTripById(tripPresentation.id)
            val trip = tripResult.data

            if (trip?.status != TripStatus.FINISHED)
                return@launch

            trip.apply {
                val tripPresentation: TripPresentationModel = TripMapper().map(trip)
                _navigateToFinished.postValue(Event(tripPresentation))
            }
        }
    }
}