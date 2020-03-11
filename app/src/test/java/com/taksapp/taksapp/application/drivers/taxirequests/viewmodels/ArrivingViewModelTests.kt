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
import com.taksapp.taksapp.domain.TripStatus
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.domain.interfaces.DriversTripsService
import com.taksapp.taksapp.domain.interfaces.DriversTripsService.TripRetrievalError
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import com.taksapp.taksapp.utils.factories.TripFactory
import com.taksapp.taksapp.utils.getOrAwaitValue
import com.taksapp.taksapp.utils.testdoubles.TaskSchedulerSpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class ArrivingViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var taxiRequestPresentation: TaxiRequestPresentationModel
    private lateinit var driversTaxiRequestServiceMock: DriversTaxiRequestService
    private lateinit var driversTripsServiceMock: DriversTripsService
    private lateinit var taskSchedulerMock: TaskScheduler
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        val taxiRequestBuilder = TaxiRequestFactory
            .withBuilder()
            .withStatus(Status.ACCEPTED)

        taxiRequestPresentation = TaxiRequestMapper().map(taxiRequestBuilder.build())
        taskSchedulerMock = mock()
        driversTaxiRequestServiceMock = mock()
        driversTripsServiceMock = mock()
        contextMock = mock()
    }

    @Test
    fun announcesArrival() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
                taxiRequestPresentation,
                arrivingViewModel.navigateToArrivedEvent.getOrAwaitValue().peekContent()
            )

            verify(driversTaxiRequestServiceMock, times(1)).announceArrival()
        }
    }

    @Test
    fun failsToAnnounceArrivalDueToInternetFailure() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

            val taxiRequestInArrivedState = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestPresentation.id)
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
                taxiRequestPresentation,
                arrivingViewModel.navigateToArrivedEvent.getOrAwaitValue().peekContent()
            )

            verify(driversTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }

    @Test
    fun navigatesToMainWhenAnnouncementFailsDueToCurrentTaxiBeingDifferentFromUserInterfaceOne() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivingViewModel = buildViewModel(taxiRequest = taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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
            val arrivingViewModel = buildViewModel(taxiRequestPresentation)

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

    @Test
    fun navigatesToArrivedWhenSynchronizedByPush() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestPresentation.id)
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val arrivingViewModel = buildViewModel()

            // Act
            arrivingViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(synchronizedTaxiRequest.id))

            // Assert
            Assert.assertEquals(
                false,
                arrivingViewModel.navigateToArrivedEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToArrivedWhenSynchronizedByPull() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestPresentation.id)
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerSpy = TaskSchedulerSpy()
            val arrivingViewModel = buildViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(ArrivingViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            Assert.assertEquals(
                false,
                arrivingViewModel.navigateToArrivedEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToMainWhenSynchronizedByPushAsCancelled() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestPresentation.id)
                .withStatus(Status.CANCELLED)
                .build()

            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))
            whenever(driversTaxiRequestServiceMock.getTaxiRequestById(synchronizedTaxiRequest.id))
                .thenReturn(Result.success(synchronizedTaxiRequest))
            whenever(contextMock.getString(R.string.text_taxi_request_already_cancelled))
                .thenReturn("text_taxi_request_already_cancelled")

            val arrivingViewModel = buildViewModel()

            // Act
            arrivingViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(synchronizedTaxiRequest.id))

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivingViewModel.navigateToMainWithErrorEvent.value?.getContentIfNotHandled()
            )
        }
    }

    @Test
    fun navigatesToMainWhenSynchronizedByPullAsCancelled() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestPresentation.id)
                .withStatus(Status.CANCELLED)
                .build()

            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))
            whenever(driversTaxiRequestServiceMock.getTaxiRequestById(synchronizedTaxiRequest.id))
                .thenReturn(Result.success(synchronizedTaxiRequest))
            whenever(contextMock.getString(R.string.text_taxi_request_already_cancelled))
                .thenReturn("text_taxi_request_already_cancelled")

            val taskSchedulerSpy = TaskSchedulerSpy()
            val arrivingViewModel = buildViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(ArrivingViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivingViewModel.navigateToMainWithErrorEvent.value?.getContentIfNotHandled()
            )
        }
    }

    @Test
    fun navigatesToTripWhenSynchronizedByPushWithStartedTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedStartedTrip = TripFactory.withBuilder()
                .withId("any-id")
                .withStatus(TripStatus.STARTED)
                .build();

            whenever(driversTripsServiceMock.getCurrentTrip())
                .thenReturn(Result.success(synchronizedStartedTrip))

            val arrivingViewModel = buildViewModel()

            // Act
            arrivingViewModel.onTripStatusChanged(
                TripStatusChangedEvent(synchronizedStartedTrip.id))

            // Assert
            Assert.assertNotNull(
                arrivingViewModel.navigateToTripEvent.value?.getContentIfNotHandled()
            )
        }
    }

    @Test
    fun doesNotNavigateToTripWhenSynchronizedByPushWithNoActiveTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedStartedTrip = TripFactory.withBuilder()
                .withId("any-id")
                .withStatus(TripStatus.FINISHED)
                .build();

            whenever(driversTripsServiceMock.getCurrentTrip())
                .thenReturn(Result.error(TripRetrievalError.TRIP_NOT_FOUND))

            val arrivingViewModel = buildViewModel()

            // Act
            arrivingViewModel.onTripStatusChanged(
                TripStatusChangedEvent(synchronizedStartedTrip.id))

            // Assert
            Assert.assertNull(arrivingViewModel.navigateToArrivedEvent.value)
        }
    }

    @Test
    fun navigatesToTripWhenSynchronizedByPullWithStartedTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedStartedTrip = TripFactory.withBuilder()
                .withId("any-id")
                .withStatus(TripStatus.STARTED)
                .build();

            whenever(driversTripsServiceMock.getCurrentTrip())
                .thenReturn(Result.success(synchronizedStartedTrip))

            val taskSchedulerSpy = TaskSchedulerSpy()
            val arrivingViewModel = buildViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(ArrivingViewModel.TRIP_PULL_TASK_ID)

            // Assert
            Assert.assertNotNull(
                arrivingViewModel.navigateToTripEvent.value?.getContentIfNotHandled()
            )
        }
    }

    @Test
    fun doesNotNavigateToTripWhenSynchronizedByPullWithWithNoActiveTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val synchronizedStartedTrip = TripFactory.withBuilder()
                .withId("any-id")
                .withStatus(TripStatus.STARTED)
                .build();

            whenever(driversTripsServiceMock.getCurrentTrip())
                .thenReturn(Result.error(TripRetrievalError.TRIP_NOT_FOUND))

            val taskSchedulerSpy = TaskSchedulerSpy()
            val arrivingViewModel = buildViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(ArrivingViewModel.TRIP_PULL_TASK_ID)

            // Assert
            Assert.assertNull(arrivingViewModel.navigateToArrivedEvent.value)
        }
    }

    private fun buildViewModel(
        taxiRequest: TaxiRequestPresentationModel? = null,
        taskScheduler: TaskScheduler? = null
    ) = ArrivingViewModel(
        taxiRequestPresentationModel = taxiRequest ?: this.taxiRequestPresentation,
        driversTaxiRequestService = driversTaxiRequestServiceMock,
        driversTripsService = driversTripsServiceMock,
        taskScheduler = taskScheduler ?: this.taskSchedulerMock,
        context = contextMock
    )
}