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
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.events.IncomingTaxiRequestEvent
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.DriversService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
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
class DriverMainViewModel(
    private val driversService: DriversService,
    private val driversTaxiRequestService: DriversTaxiRequestService,
    private val devicesService: DevicesService,
    private val pushNotificationTokenRetriever: PushNotificationTokenRetriever,
    private val taskScheduler: TaskScheduler,
    private val context: Context
): ViewModel() {
    companion object {
        const val ONLINE_COUNTDOWN_TASK = "ONLINE_COUNTDOWN_TASK"
    }

    private val _switchingDriverStatus = MutableLiveData<Boolean>()
    private val _isDriverOnline = MutableLiveData<Boolean>()
    private val _showIncomingTaxiRequestEvent = MutableLiveData<Event<TaxiRequestPresentationModel>>()
    private val _snackBarErrorEvent = MutableLiveData<Event<String>>()

    val switchingDriverStatus: LiveData<Boolean> = _switchingDriverStatus
    val isDriverOnline: LiveData<Boolean> = _isDriverOnline
    val showIncomingTaxiRequestEvent: LiveData<Event<TaxiRequestPresentationModel>> =
        _showIncomingTaxiRequestEvent
    val snackBarErrorEvent: MutableLiveData<Event<String>> = _snackBarErrorEvent

    init {
        _isDriverOnline.observeForever { isOnline ->
            if (isOnline) {
                taskScheduler.schedule(
                    ONLINE_COUNTDOWN_TASK,
                    1.toDuration(TimeUnit.MINUTES)
                ) { switchToOnline() }
            } else {
                taskScheduler.cancel(ONLINE_COUNTDOWN_TASK)
            }
        }

        EventBus.getDefault().register(this)
    }

    fun switchToOnline() {
        _switchingDriverStatus.value = true

        viewModelScope.launch {
            var triesCount = 0

            while (true) {
                try {
                    val result = driversService.setAsOnline()
                    triesCount++

                    if (hasStatusSetReachedMaxTries(triesCount, result)) {
                        _isDriverOnline.postValue(false)
                        break
                    }

                    if (result.isSuccessful) {
                        _isDriverOnline.postValue(true)
                        break
                    } else if (driverHasNoDevice(result)) {
                        registerUserDevice()
                        continue
                    } else if (failedDueToServerError(result)) {
                        _isDriverOnline.postValue(false)
                        _snackBarErrorEvent
                            .postValue(Event(context.getString(R.string.text_server_error)))
                        break
                    }
                } catch (e: IOException) {
                    _isDriverOnline.postValue(false)
                    _snackBarErrorEvent
                        .postValue(Event(context.getString(R.string.text_internet_error)))
                    break
                } finally {
                    _switchingDriverStatus.postValue(false)
                }
            }
        }
    }

    fun switchToOffline() {
        _switchingDriverStatus.value = true

        viewModelScope.launch {
            try {
                val result = driversService.setAsOffline()
                if (result.hasFailed) {
                    _snackBarErrorEvent
                        .postValue(Event(context.getString(R.string.text_server_error)))
                }
            } catch (e: IOException) {
                _snackBarErrorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                _switchingDriverStatus.postValue(false)
                _isDriverOnline.postValue(false)
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onTaxiRequestReceived(incomingTaxiRequestEvent: IncomingTaxiRequestEvent) {
        viewModelScope.launch {
            val result = driversTaxiRequestService
                .getTaxiRequestById(incomingTaxiRequestEvent.taxiRequestId)

            if (result.hasFailed)
                return@launch

            val taxiRequest = result.data

            taxiRequest?.let {
                if (taxiRequest.hasExpired() || taxiRequest.status != Status.WAITING_ACCEPTANCE)
                    return@launch

                _showIncomingTaxiRequestEvent.postValue(Event(TaxiRequestMapper().map(taxiRequest)))
            }
        }
    }

    private fun hasStatusSetReachedMaxTries(
        triesCount: Int,
        result: Result<Nothing, DriversService.OnlineSwitchError>
    ): Boolean {
        val maxTries = 2
        return triesCount > maxTries && result.hasFailed
    }

    private suspend fun registerUserDevice(): Result<Nothing, String> {
        val pushNotificationResult =
            pushNotificationTokenRetriever.getPushNotificationToken()
        if (pushNotificationResult.hasFailed)
            return Result.error(pushNotificationResult.error)

        return devicesService
            .registerUserDevice(pushNotificationResult.data!!, DevicesService.Platform.ANDROID)
    }

    private fun failedDueToServerError(result: Result<Nothing, DriversService.OnlineSwitchError>) =
        result.error == DriversService.OnlineSwitchError.SERVER_ERROR

    private fun driverHasNoDevice(result: Result<Nothing, DriversService.OnlineSwitchError>) =
        result.hasFailed && result.error == DriversService.OnlineSwitchError.DRIVER_HAS_NO_DEVICE
}