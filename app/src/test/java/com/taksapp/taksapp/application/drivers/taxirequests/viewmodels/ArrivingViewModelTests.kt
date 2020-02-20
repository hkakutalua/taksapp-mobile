package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.application.shared.mappers.TaxiRequestMapper
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import com.taksapp.taksapp.utils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException

@ExperimentalCoroutinesApi
class ArrivingViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var taxiRequest: TaxiRequestPresentationModel
    private lateinit var driversTaxiRequestServiceMock: DriversTaxiRequestService
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        val taxiRequestBuilder = TaxiRequestFactory
            .withBuilder()
            .withStatus(Status.ACCEPTED)

        taxiRequest = TaxiRequestMapper().map(taxiRequestBuilder.build())
        driversTaxiRequestServiceMock = mock()
        contextMock = mock()
    }

    @Test
    fun announcesArrival() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenReturn(Result.success(null))

            coroutineScopeRule.pauseDispatcher()

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(true, arrivingViewModel.processing.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertEquals(false, arrivingViewModel.processing.getOrAwaitValue())

            Assert.assertEquals(
                taxiRequest,
                arrivingViewModel.navigateToArrivedEvent.getOrAwaitValue().peekContent()
            )

            verify(driversTaxiRequestServiceMock, times(1)).announceArrival()
        }
    }

    @Test
    fun failsToAnnounceArrivalDueToInternetFailure() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(
                "internet_error",
                arrivingViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToAnnounceArrivalDueToServerIssues() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenReturn(Result.error(TaxiRequestArrivalAnnounceError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error")

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(
                "server_error",
                arrivingViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun navigatesToMainWhenAnnounceArrivalDoesNotFindTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenReturn(Result.error(TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_FOUND))
            whenever(contextMock.getString(R.string.text_taxi_request_already_cancelled))
                .thenReturn("taxi_request_already_cancelled_error")

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(
                "taxi_request_already_cancelled_error",
                arrivingViewModel.navigateToMainWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun navigatesToArrivedWhenAnnouncementFailsDueToCurrentTaxiRequestInArrivedState() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            val taxiRequestInArrivedState = TaxiRequestFactory.withBuilder()
                .withId(taxiRequest.id)
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenReturn(Result.error(TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(taxiRequestInArrivedState))

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(
                taxiRequest,
                arrivingViewModel.navigateToArrivedEvent.getOrAwaitValue().peekContent()
            )

            verify(driversTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }

    @Test
    fun navigatesToMainWhenAnnouncementFailsDueToCurrentTaxiBeingDifferentFromUserInterfaceOne() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest = taxiRequest)

            val taxiRequestInArrivedState = TaxiRequestFactory.withBuilder()
                .withId("different-taxi-request")
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            whenever(driversTaxiRequestServiceMock.announceArrival())
                .thenReturn(Result.error(TaxiRequestArrivalAnnounceError.TAXI_REQUEST_NOT_IN_ACCEPTED_STATUS))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(taxiRequestInArrivedState))

            // Act
            arrivingViewModel.announceArrival()

            // Assert
            Assert.assertEquals(
                false,
                arrivingViewModel.navigateToMain.getOrAwaitValue().hasBeenHandled
            )

            verify(driversTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }

    @Test
    fun cancelsTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            coroutineScopeRule.pauseDispatcher()

            // Act
            arrivingViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(true, arrivingViewModel.processing.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertEquals(false, arrivingViewModel.processing.getOrAwaitValue())

            Assert.assertEquals(
                false,
                arrivingViewModel.navigateToMain.getOrAwaitValue().hasBeenHandled
            )

            verify(driversTaxiRequestServiceMock, times(1))
                .cancelCurrentTaxiRequest()
        }
    }

    @Test
    fun navigateToMainWhenTaxiRequestCancellationFailsDueToRequestNotFound() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestCancellationError.TAXI_REQUEST_NOT_FOUND))
            whenever(contextMock.getString(R.string.text_taxi_request_already_cancelled))
                .thenReturn("text_taxi_request_already_cancelled")

            // Act
            arrivingViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivingViewModel.navigateToMainWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToCancelTaxiRequestDueToInternetError() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("text_internet_error")

            // Act
            arrivingViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_internet_error",
                arrivingViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToCancelTaxiRequestDueToServerError() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestCancellationError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("text_server_error")

            // Act
            arrivingViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_server_error",
                arrivingViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    private fun buildViewModel(taxiRequest: TaxiRequestPresentationModel?) = ArrivingViewModel(
        taxiRequest = taxiRequest ?: this.taxiRequest,
        driversTaxiRequestService = driversTaxiRequestServiceMock,
        context = contextMock)
}