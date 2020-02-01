package com.taksapp.taksapp.application.taxirequest.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.*
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
import java.util.*
import kotlin.time.ExperimentalTime

@ExperimentalTime
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

        taxiRequestViewModel = buildTaxiRequestViewModel()
    }

    @Test
    fun navigateToMainWhenCountdownToTaxiRequestTimesOut() {
        coroutineScope.launch {
            // Arrange/Act
            val taxiRequestWaitingAcceptance = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()

            val taxiRequestViewModel = buildTaxiRequestViewModel(
                taxiRequestWaitingAcceptance,
                taskScheduler = taskSchedulerDummy
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
            val taxiRequestViewModel = buildTaxiRequestViewModel(acceptedTaxiRequest)

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
            val taxiRequestViewModel = buildTaxiRequestViewModel(taxiRequestWithArrivedDriver)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToAcceptedWhenSynchronizedByPush() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .withExpirationDate(DateTime.now().plusSeconds(30))
                .build()

            val synchronizedTaxiRequest =
                TaxiRequestFactory.withBuilder().withStatus(Status.ACCEPTED).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent("random-taxi-request-id"))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptedStateEvent.value?.hasBeenHandled
            )

            taskSchedulerDummy
                .assertThatHasCancelledTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
        }
    }

    @Test
    fun navigatesToAcceptedWhenSynchronizedByPull() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest =
                TaxiRequestFactory.withBuilder().withStatus(Status.WAITING_ACCEPTANCE).build()
            val synchronizedTaxiRequest =
                TaxiRequestFactory.withBuilder().withStatus(Status.ACCEPTED).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            taskSchedulerDummy.executePendingTask(TaxiRequestViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToAcceptedStateEvent.value?.hasBeenHandled
            )

            taskSchedulerDummy
                .assertThatHasCancelledTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
        }
    }

    @Test
    fun navigatesToDriverArrivedWhenSynchronizedByPush() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .build()

            val synchronizedTaxiRequest =
                TaxiRequestFactory.withBuilder().withStatus(Status.DRIVER_ARRIVED).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent("random-taxi-request-id"))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToDriverArrivedWhenSynchronizedByPull() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .build()

            val synchronizedTaxiRequest =
                TaxiRequestFactory.withBuilder().withStatus(Status.DRIVER_ARRIVED).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            taskSchedulerDummy.executePendingTask(TaxiRequestViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToMainWhenSynchronizedByPushAsCancelled() {
        coroutineScope.launch {
            // Arrange
            val taxiRequestId = UUID.randomUUID().toString()
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestId)
                .withStatus(Status.ACCEPTED)
                .build()
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(taxiRequestId)
                .withStatus(Status.CANCELLED)
                .build()

            val taxiRequestViewModel = buildTaxiRequestViewModel(initialTaxiRequest)

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))
            whenever(ridersTaxiRequestServiceMock.getTaxiRequestById(taxiRequestId))
                .thenReturn(Result.success(synchronizedTaxiRequest))

            // Act
            taxiRequestViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(synchronizedTaxiRequest.id))

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.showCancelledMessageAndNavigateBackEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun navigatesToMainWhenSynchronizedByPullAsCancelled() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .build()
            val synchronizedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.CANCELLED)
                .build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))
            whenever(ridersTaxiRequestServiceMock.getTaxiRequestById(initialTaxiRequest.id))
                .thenReturn(Result.success(synchronizedTaxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            taskSchedulerDummy.executePendingTask(TaxiRequestViewModel.TAXI_REQUEST_PULL_TASK_ID)

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
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest,
                taskScheduler = taskSchedulerDummy
            )

            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            coroutineScope.pauseDispatcher()

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            taskSchedulerDummy
                .assertThatHasPausedTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
        }
    }

    @Test
    fun countdownToTaxiRequestTimeoutResumesAfterCancellation() {
        coroutineScope.launch {
            // Arrange
            val initialTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val taskSchedulerDummy = TaskSchedulerDummy()
            val taxiRequestViewModel = buildTaxiRequestViewModel(
                initialTaxiRequest,
                taskScheduler = taskSchedulerDummy
            )

            whenever(ridersTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            // Act
            taxiRequestViewModel.cancelCurrentTaxiRequest()

            // Assert
            coroutineScope.advanceUntilIdle()
            taskSchedulerDummy
                .assertThatHasResumedTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
        }
    }

    @Test
    fun countdownToTaxiRequestTimeoutPausesDuringPullSynchronization() {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().plusSeconds(30))
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(taxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            buildTaxiRequestViewModel(taxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            coroutineScope.pauseDispatcher()
            taskSchedulerDummy
                .executePendingTask(TaxiRequestViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            taskSchedulerDummy
                .assertThatHasPausedTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
        }
    }

    @Test
    fun countdownToTaxiRequestTimeoutResumesAfterPullSynchronization() {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().plusSeconds(30))
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(taxiRequest))

            val taskSchedulerDummy = TaskSchedulerDummy()
            buildTaxiRequestViewModel(taxiRequest, taskScheduler = taskSchedulerDummy)

            // Act
            coroutineScope.pauseDispatcher()
            taskSchedulerDummy
                .executePendingTask(TaxiRequestViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            coroutineScope.advanceUntilIdle()

            taskSchedulerDummy
                .assertThatHasResumedTask(TaxiRequestViewModel.TAXI_REQUEST_TIMEOUT_TASK_ID)
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

    private fun buildTaxiRequestViewModel(
        initialTaxiRequest: TaxiRequest? = null,
        riderTaxiRequestsRepository: RiderTaxiRequestsRepository? = null,
        taskScheduler: TaskScheduler? = null,
        context: Context? = null): TaxiRequestViewModel {

        val taxiRequestInInitialStatus = TaxiRequestFactory.withBuilder()
            .withStatus(Status.WAITING_ACCEPTANCE)
            .build()

        return TaxiRequestViewModel(
            initialTaxiRequest ?: taxiRequestInInitialStatus,
            riderTaxiRequestsRepository ?: this.riderTaxiRequestsRepository,
            taskScheduler ?: this.taskSchedulerMock,
            context ?: this.contextMock
        )
    }
}