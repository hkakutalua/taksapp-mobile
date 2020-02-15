package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.whenever
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
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
class TaxiRequestAcceptanceViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var driversTaxiRequestServiceMock: DriversTaxiRequestService
    private lateinit var taskScheduler: TaskScheduler
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        driversTaxiRequestServiceMock = mock()
        taskScheduler = mock()
        contextMock = mock()
    }
    
    @Test
    fun startsCountdownToAutomaticallyDenyTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange/Act
            val taskSchedulerSpy = TaskSchedulerSpy()
            buildTaxiRequestAcceptanceViewModel(taskScheduler = taskSchedulerSpy)

            // Assert
            taskSchedulerSpy.assertThatHasResumedTask(
                TaxiRequestAcceptanceViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
        }
    }

    @Test
    fun navigateToMainScreenWhenCountdownExpires() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val taxiRequestAcceptanceViewModel =
                buildTaxiRequestAcceptanceViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(
                TaxiRequestAcceptanceViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestAcceptanceViewModel.navigateToMainScreen.getOrAwaitValue().hasBeenHandled
            )
        }
    }

    @Test
    fun acceptsTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

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
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertTrue(taxiRequestAcceptanceViewModel.acceptingTaxiRequest.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertFalse(taxiRequestAcceptanceViewModel.acceptingTaxiRequest.getOrAwaitValue())
            
            Assert.assertEquals(
                false,
                taxiRequestAcceptanceViewModel.navigateToTaxiRequestEvent.value?.hasBeenHandled
            )

            inOrder(driversTaxiRequestServiceMock) {
                verify(driversTaxiRequestServiceMock, times(1))
                    .acceptTaxiRequest("random-taxi-id")
                verify(driversTaxiRequestServiceMock, times(1)).getCurrentTaxiRequest()
            }
        }
    }

    @Test
    fun failsToAcceptTaxiRequestDueToInternetError() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "internet_error",
                taxiRequestAcceptanceViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToAcceptTaxiRequestDueToServerError() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(DriversTaxiRequestService.TaxiRequestAcceptanceError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error")

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "server_error",
                taxiRequestAcceptanceViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToAcceptTaxiRequestAlreadyAcceptedByAnotherDriver() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(DriversTaxiRequestService.TaxiRequestAcceptanceError.TAXI_REQUEST_ALREADY_ACCEPTED_BY_ANOTHER_DRIVER))
            whenever(contextMock.getString(R.string.error_taxi_request_accepted_by_another_driver))
                .thenReturn("taxi_request_already_accepted_error")

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "taxi_request_already_accepted_error",
                taxiRequestAcceptanceViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun navigatesToTaxiRequestWhenAcceptingOneAlreadyAcceptedByMyself() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(DriversTaxiRequestService.TaxiRequestAcceptanceError.TAXI_REQUEST_ALREADY_ACCEPTED_BY_YOU))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestAcceptanceViewModel.navigateToTaxiRequestEvent.value?.hasBeenHandled
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
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.error(DriversTaxiRequestService.TaxiRequestAcceptanceError.TAXI_REQUEST_EXPIRED))
            whenever(contextMock.getString(R.string.error_taxi_request_acceptance_failed_due_to_expiry))
                .thenReturn("error_taxi_request_acceptance_failed_due_to_expiry")

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            Assert.assertEquals(
                "error_taxi_request_acceptance_failed_due_to_expiry",
                taxiRequestAcceptanceViewModel.navigateToMainScreenWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun cancelsOnlineCountdownAfterAcceptingTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val taxiRequestAcceptanceViewModel = buildTaxiRequestAcceptanceViewModel(taskScheduler = taskSchedulerSpy)

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.ACCEPTED)
                .withExpirationDate(DateTime.now().plusSeconds(12))
                .build()

            whenever(driversTaxiRequestServiceMock.acceptTaxiRequest("random-taxi-id"))
                .thenReturn(Result.success(null))
            whenever(driversTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            // Act
            taxiRequestAcceptanceViewModel.acceptTaxiRequest("random-taxi-id")

            // Assert
            taskSchedulerSpy.assertThatHasCancelledTask(
                TaxiRequestAcceptanceViewModel.TAXI_REQUEST_DENIAL_COUNTDOWN_TASK_ID)
        }
    }

    private fun buildTaxiRequestAcceptanceViewModel(
        driversTaxiRequestService: DriversTaxiRequestService? = null,
        taskScheduler: TaskScheduler? = null,
        context: Context? = null
    ): TaxiRequestAcceptanceViewModel {
        return TaxiRequestAcceptanceViewModel(
            driversTaxiRequestService = driversTaxiRequestService ?: this.driversTaxiRequestServiceMock,
            taskScheduler = taskScheduler ?: this.taskScheduler,
            context = context ?: this.contextMock
        )
    }
}