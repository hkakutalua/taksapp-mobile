package com.taksapp.taksapp.ui.taxi.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.PushNotificationTokenRetriever
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.ui.taxi.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class TaxiRequestViewModelTests {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var ridersTaxiRequestServiceMock: RidersTaxiRequestService
    private lateinit var devicesServiceMock: DevicesService
    private lateinit var pushNotificationTokenRetrieverMock: PushNotificationTokenRetriever
    private lateinit var contextMock: Context
    private lateinit var taxiRequestViewModel: TaxiRequestViewModel

    @Before
    fun beforeEachTest() {
        ridersTaxiRequestServiceMock = mock()
        devicesServiceMock = mock()
        pushNotificationTokenRetrieverMock = mock()
        contextMock = mock()
        val taxiRequestsRepository =
            RiderTaxiRequestsRepository(
                ridersTaxiRequestServiceMock,
                devicesServiceMock,
                pushNotificationTokenRetrieverMock
            )
        taxiRequestViewModel = TaxiRequestViewModel(taxiRequestsRepository, contextMock)
    }

    @Test
    fun sendsTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.success(TaxiRequest(Status.WAITING_ACCEPTANCE)))
            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)
            coroutineScope.pauseDispatcher()

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(true, taxiRequestViewModel.sendingTaxiRequest.value)

            coroutineScope.advanceUntilIdle()
            Assert.assertEquals(false, taxiRequestViewModel.sendingTaxiRequest.value)
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptanceWaitEvent.getOrAwaitValue().hasBeenHandled
            )

            verify(ridersTaxiRequestServiceMock)
                .sendTaxiRequest(Location(0.28394, 1.02934), Location(0.28394, 1.02934))
        }
    }

    @Test
    fun failsToSendRequestDueToNetworkFailure() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")
            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(false, taxiRequestViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                "internet_error",
                taxiRequestViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun taxiRequestFailsWhenTheresNoAvailableDrivers() {
        coroutineScope.launch {
            // Arrange
            whenever(contextMock.getString(R.string.error_no_available_drivers))
                .thenReturn("no_available_drivers")
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.NO_AVAILABLE_DRIVERS))
            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(false, taxiRequestViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                "no_available_drivers",
                taxiRequestViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun registersDriverDeviceWhenTaxiRequestFails() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.DEVICE_NOT_REGISTERED))
                .thenReturn(Result.success(TaxiRequest(Status.WAITING_ACCEPTANCE)))
            whenever(pushNotificationTokenRetrieverMock.getPushNotificationToken())
                .thenReturn(Result.success("push-notification-token"))
            whenever(devicesServiceMock.registerUserDevice(any(), any()))
                .thenReturn(Result.success(null))

            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(false, taxiRequestViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptanceWaitEvent.getOrAwaitValue().hasBeenHandled
            )

            inOrder(ridersTaxiRequestServiceMock, devicesServiceMock, ridersTaxiRequestServiceMock) {
                verify(ridersTaxiRequestServiceMock)
                    .sendTaxiRequest(Location(0.28394, 1.02934), Location(0.28394, 1.02934))
                verify(devicesServiceMock)
                    .registerUserDevice("push-notification-token", DevicesService.Platform.ANDROID)
                verify(ridersTaxiRequestServiceMock)
                    .sendTaxiRequest(Location(0.28394, 1.02934), Location(0.28394, 1.02934))
            }
        }
    }

    @Test
    fun navigatesToWaitScreenWhenRequestFailsDueToActiveOneInWaitState() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS))
            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(TaxiRequest(Status.WAITING_ACCEPTANCE)))

            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptanceWaitEvent.getOrAwaitValue().hasBeenHandled
            )
            verify(ridersTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }

    @Test
    fun navigatesToAcceptedScreenWhenRequestFailsDueToActiveOneInAcceptedState() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS))
            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(TaxiRequest(Status.ACCEPTED)))

            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptedEvent.getOrAwaitValue().hasBeenHandled
            )
            verify(ridersTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }

    @Test
    fun navigatesToDriverArrivedScreenWhenRequestFailsDueToActiveOneInDriverArrivedState() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS))
            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(TaxiRequest(Status.DRIVER_ARRIVED)))

            val origin = LocationPresentationModel(0.28394, 1.02934)
            val destination = LocationPresentationModel(0.28394, 1.02934)

            // Act
            taxiRequestViewModel.sendTaxiRequest(origin, destination)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedEvent.getOrAwaitValue().hasBeenHandled
            )
            verify(ridersTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }


    @Test
    fun cancelsCurrentTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))
            coroutineScope.pauseDispatcher()

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertEquals(true, taxiRequestViewModel.cancellingTaxiRequest.getOrAwaitValue())

            coroutineScope.advanceUntilIdle()
            Assert.assertEquals(false, taxiRequestViewModel.cancellingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToMainEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToMainScreenWhenTaxiRequestToBeCancelledIsNotActive() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(CancellationError.TAXI_REQUEST_NOT_FOUND))

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToMainEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun taxiRequestCancellationFailsDueToNetworkFailure() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            verify(ridersTaxiRequestServiceMock).cancelCurrentTaxiRequest()
            Assert.assertEquals(
                "internet_error",
                taxiRequestViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }
}