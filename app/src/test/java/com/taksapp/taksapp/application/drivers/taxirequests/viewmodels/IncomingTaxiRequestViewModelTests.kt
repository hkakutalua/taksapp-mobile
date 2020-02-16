package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestAcceptanceError.*
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import com.taksapp.taksapp.utils.factories.TaxiRequestPresentationModelFactory
import com.taksapp.taksapp.utils.getOrAwaitValue
import com.taksapp.taksapp.utils.testdoubles.TaskSchedulerSpy
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.IOException
import kotlin.time.ExperimentalTime

@ExperimentalTime
@ExperimentalCoroutinesApi
class IncomingTaxiRequestViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var taxiRequest: TaxiRequestPresentationModel
    private lateinit var driversTaxiRequestServiceMock: DriversTaxiRequestService
    private lateinit var taskScheduler: TaskScheduler
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        taxiRequest = TaxiRequestPresentationModelFactory.withBuilder().build()
        driversTaxiRequestServiceMock = mock()
        taskScheduler = mock()
        contextMock = mock()
    }
    
    @Test
    fun startsCountdownToAutomaticallyDenyTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange/Act
            val taskSchedulerSpy = TaskSchedulerSpy()
            val incomingTaxiRequestViewModel =
                buildIncomingTaxiRequestViewModel(taskScheduler = taskSchedulerSpy)

            // Assert
            taskSchedulerSpy.assertThatHasResumedTask(
                IncomingTaxiRequestViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)

            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.startTaxiRequestSecondsCountdownEvent
                    .getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun navigateToMainScreenWhenCountdownExpires() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val incomingTaxiRequestViewModel =
                buildIncomingTaxiRequestViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(
                IncomingTaxiRequestViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)

            // Assert
            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.navigateToMainScreen.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun acceptsTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.success(null))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            coroutineScopeRule.pauseDispatcher()

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertTrue(incomingTaxiRequestViewModel.acceptingTaxiRequest.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertFalse(incomingTaxiRequestViewModel.acceptingTaxiRequest.getOrAwaitValue())
            
            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.navigateToTaxiRequestEvent.value?.hasBeenHandled
            )

            inOrder(driversTaxiRequestServiceMock) {
                verify(driversTaxiRequestServiceMock, times(1))
                    .acceptTaxiRequest("random-taxi-id")
                verify(driversTaxiRequestServiceMock, times(1)).getCurrentTaxiRequest()
            }
        }
    }
    
    @Test
    fun acceptingTaxiRequestPausesCountdown() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val incomingTaxiRequestViewModel = 
                buildIncomingTaxiRequestViewModel(taskScheduler = taskSchedulerSpy)

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.success(null))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            coroutineScopeRule.pauseDispatcher()

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")
            
            // Assert
            taskSchedulerSpy.assertThatHasPausedTask(
                IncomingTaxiRequestViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)

            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.pauseTaxiRequestCountdownEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun countdownResumesAfterTaxiRequestAcceptance() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val incomingTaxiRequestViewModel =
                buildIncomingTaxiRequestViewModel(taskScheduler = taskSchedulerSpy)

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.success(null))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            coroutineScopeRule.pauseDispatcher()

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            coroutineScopeRule.advanceUntilIdle()

            taskSchedulerSpy.assertThatHasResumedTask(
                IncomingTaxiRequestViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)

            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.resumeTaxiRequestCountdownEvent.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun failsToAcceptTaxiRequestDueToInternetError() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "internet_error",
                incomingTaxiRequestViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToAcceptTaxiRequestDueToServerError() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error")

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "server_error",
                incomingTaxiRequestViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToAcceptTaxiRequestAlreadyAcceptedByAnotherDriver() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER))
            whenever(contextMock.getString(R.string.error_taxi_request_accepted_by_another_driver))
                .thenReturn("taxi_request_already_accepted_error")

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "taxi_request_already_accepted_error",
                incomingTaxiRequestViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun navigatesToTaxiRequestWhenAcceptingOneAlreadyAcceptedByMyself() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                false,
                incomingTaxiRequestViewModel.navigateToTaxiRequestEvent.value?.hasBeenHandled
            )

            inOrder(driversTaxiRequestServiceMock) {
                verify(driversTaxiRequestServiceMock, times(1))
                    .acceptTaxiRequest("random-taxi-id")
                verify(driversTaxiRequestServiceMock, times(1)).getCurrentTaxiRequest()
            }
        }
    }

    @Test
    fun failsToAcceptTaxiRequestAlreadyExpired() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequestViewModel = buildIncomingTaxiRequestViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(TAXI_REQUEST_EXPIRED))
            whenever(contextMock.getString(R.string.error_taxi_request_acceptance_failed_due_to_expiry))
                .thenReturn("error_taxi_request_acceptance_failed_due_to_expiry")

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "error_taxi_request_acceptance_failed_due_to_expiry",
                incomingTaxiRequestViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun cancelsOnlineCountdownAfterAcceptingTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val incomingTaxiRequestViewModel =
                buildIncomingTaxiRequestViewModel(taskScheduler = taskSchedulerSpy)

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.success(null))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            // Act
            incomingTaxiRequestViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            taskSchedulerSpy.assertThatHasCancelledTask(
                IncomingTaxiRequestViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
        }
    }

    private fun buildIncomingTaxiRequestViewModel(
        driversTaxiRequestService: DriversTaxiRequestService? = null,
        taskScheduler: TaskScheduler? = null,
        context: Context? = null
    ): IncomingTaxiRequestViewModel {
        return IncomingTaxiRequestViewModel(
            taxiRequest = taxiRequest,
            driversTaxiRequestService = driversTaxiRequestService ?: this.driversTaxiRequestServiceMock,
            taskScheduler = taskScheduler ?: this.taskScheduler,
            context = context ?: this.contextMock
        )
    }
}