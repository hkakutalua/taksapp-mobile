package com.taksapp.taksapp.application.taxirequest.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.FareRepository
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import com.taksapp.taksapp.utils.getOrAwaitValue
import com.taksapp.taksapp.utils.testdoubles.TaskSchedulerDummy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.joda.time.DateTime
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

    private lateinit var fareRepositoryMock: FareRepository
    private lateinit var ridersTaxiRequestServiceMock: RidersTaxiRequestService
    private lateinit var taskSchedulerMock: TaskScheduler
    private lateinit var contextMock: Context
    private lateinit var riderTaxiRequestsRepository: RiderTaxiRequestsRepository
    private lateinit var taxiRequestViewModel: TaxiRequestViewModel

    @Before
    fun beforeEachTest() {
        fareRepositoryMock = mock()
        ridersTaxiRequestServiceMock = mock()
        taskSchedulerMock = mock()
        contextMock = mock()
        riderTaxiRequestsRepository =
            RiderTaxiRequestsRepository(ridersTaxiRequestServiceMock, mock(), mock())

        val taxiRequestInInitialStatus = TaxiRequestFactory.withBuilder()
            .withStatus(Status.WAITING_ACCEPTANCE)
            .build()

        taxiRequestViewModel = TaxiRequestViewModel(
            taxiRequestInInitialStatus, riderTaxiRequestsRepository, taskSchedulerMock, contextMock)
    }

    @Test
    fun navigateToMainWhenCountdownToTaxiRequestTimesOut() {
        coroutineScope.launch {
            // Arrange/Act
            val taxiRequestWaitingAcceptance = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()

            taxiRequestViewModel = TaxiRequestViewModel(
                taxiRequestWaitingAcceptance,
                riderTaxiRequestsRepository,
                taskSchedulerDummy,
                contextMock
            )

            // Assert
            Assert.assertNull(
                taxiRequestViewModel.showTimeoutMessageAndNavigateBackEvent.value)

            taskSchedulerDummy.executePendingTasks()

            Assert.assertFalse(
                taxiRequestViewModel.showTimeoutMessageAndNavigateBackEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToAcceptedStateWhenTaxiRequestIsInAcceptedState() {
        coroutineScope.launch {
            // Arrange
            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .build()

            // Act
            val taxiRequestViewModel = TaxiRequestViewModel(
                acceptedTaxiRequest,
                riderTaxiRequestsRepository,
                taskSchedulerMock,
                contextMock
            )

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToDriverArrivedStateWhenTaxiRequestIsInDriverArrivedState() {
        coroutineScope.launch {
            // Arrange
            val taxiRequestWithArrivedDriver = TaxiRequestFactory.withBuilder()
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            // Act
            val taxiRequestViewModel = TaxiRequestViewModel(
                taxiRequestWithArrivedDriver,
                riderTaxiRequestsRepository,
                taskSchedulerMock,
                contextMock
            )

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToAcceptedStateWhenNotified() {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().plusSeconds(30))
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = TaxiRequestViewModel(
                taxiRequest, riderTaxiRequestsRepository, taskSchedulerDummy, contextMock
            )
            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .build()

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(acceptedTaxiRequest))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptedStateEvent.value?.hasBeenHandled
            )

            taskSchedulerDummy.assertThatHasCancelledTask()
        }
    }

    @Test
    fun navigatesToDriverArrivedStateWhenNotified() {
        coroutineScope.launch {
            // Arrange
            val taxiRequestViewModel = TaxiRequestViewModel(
                null, riderTaxiRequestsRepository, taskSchedulerMock, contextMock
            )
            val driverArrivedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.DRIVER_ARRIVED)
                .build()

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(driverArrivedTaxiRequest))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToMainWhenNotifiedWithCancelledState() {
        coroutineScope.launch {
            // Arrange
            val taxiRequestViewModel = TaxiRequestViewModel(
                null, riderTaxiRequestsRepository, taskSchedulerMock, contextMock
            )
            val cancelledTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.CANCELLED)
                .build()

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(cancelledTaxiRequest))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.showCancelledMessageAndNavigateBackEvent.value?.hasBeenHandled
            )
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
                taxiRequestViewModel.navigateBackEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun countdownToTaxiRequestTimeoutPausesDuringCancellation() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            val taxiRequestInInitialStatus = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = TaxiRequestViewModel(
                taxiRequestInInitialStatus, riderTaxiRequestsRepository, taskSchedulerDummy, contextMock
            )
            coroutineScope.pauseDispatcher()

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            taskSchedulerDummy.assertThatHasPausedTask()
        }
    }

    @Test
    fun countdownToTaxiRequestTimeoutResumesAfterCancellation() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            val taxiRequestInInitialStatus = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = TaxiRequestViewModel(
                taxiRequestInInitialStatus, riderTaxiRequestsRepository, taskSchedulerDummy, contextMock
            )
            coroutineScope.pauseDispatcher()

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            coroutineScope.advanceUntilIdle()
            taskSchedulerDummy.assertThatHasResumedTask()
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
                taxiRequestViewModel.navigateBackEvent.getOrAwaitValue().hasBeenHandled
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

    @Test
    fun taxiRequestCancellationFailsDueToServerError() {
        coroutineScope.launch {
            // Arrange
            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(CancellationError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error")

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            verify(ridersTaxiRequestServiceMock).cancelCurrentTaxiRequest()
            Assert.assertEquals(
                "server_error",
                taxiRequestViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }
}