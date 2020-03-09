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
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.*
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
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
class ArrivedViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var taxiRequestPresentation: TaxiRequestPresentationModel
    private lateinit var driversTaxiRequestServiceMock: DriversTaxiRequestService
    private lateinit var taskSchedulerMock: TaskScheduler
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        val taxiRequestBuilder = TaxiRequestFactory
            .withBuilder()
            .withStatus(Status.DRIVER_ARRIVED)

        taxiRequestPresentation = TaxiRequestMapper().map(taxiRequestBuilder.build())
        taskSchedulerMock = mock()
        driversTaxiRequestServiceMock = mock()
        contextMock = mock()
    }

    @Test
    fun cancelsTaxiRequest() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivedViewModel = buildViewModel(taxiRequestPresentation)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.success(null))

            coroutineScopeRule.pauseDispatcher()

            // Act
            arrivedViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(true, arrivedViewModel.processing.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertEquals(false, arrivedViewModel.processing.getOrAwaitValue())

            Assert.assertEquals(
                false,
                arrivedViewModel.navigateToMain.getOrAwaitValue().hasBeenHandled
            )

            verify(driversTaxiRequestServiceMock, times(1))
                .cancelCurrentTaxiRequest()
        }
    }

    @Test
    fun navigateToMainWhenTaxiRequestCancellationFailsDueToRequestNotFound() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivedViewModel = buildViewModel(taxiRequestPresentation)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestCancellationError.TAXI_REQUEST_NOT_FOUND))
            whenever(contextMock.getString(R.string.text_taxi_request_already_cancelled))
                .thenReturn("text_taxi_request_already_cancelled")

            // Act
            arrivedViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivedViewModel.navigateToMainWithErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToCancelTaxiRequestDueToInternetError() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivedViewModel = buildViewModel(taxiRequestPresentation)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("text_internet_error")

            // Act
            arrivedViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_internet_error",
                arrivedViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToCancelTaxiRequestDueToServerError() {
        coroutineScopeRule.launch {
            // Arrange
            val arrivedViewModel = buildViewModel(taxiRequestPresentation)

            whenever(driversTaxiRequestServiceMock.cancelCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestCancellationError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("text_server_error")

            // Act
            arrivedViewModel.cancelTaxiRequest()

            // Assert
            Assert.assertEquals(
                "text_server_error",
                arrivedViewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
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

            val arrivedViewModel = buildViewModel()

            // Act
            arrivedViewModel.onTaxiRequestStatusChanged(
                TaxiRequestStatusChangedEvent(synchronizedTaxiRequest.id))

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivedViewModel.navigateToMainWithErrorEvent.value?.getContentIfNotHandled()
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
            val arrivedViewModel = buildViewModel(taskScheduler = taskSchedulerSpy)

            // Act
            taskSchedulerSpy.executePendingTask(ArrivedViewModel.TAXI_REQUEST_PULL_TASK_ID)

            // Assert
            Assert.assertEquals(
                "text_taxi_request_already_cancelled",
                arrivedViewModel.navigateToMainWithErrorEvent.value?.getContentIfNotHandled()
            )
        }
    }

    private fun buildViewModel(
        taxiRequest: TaxiRequestPresentationModel? = null,
        taskScheduler: TaskScheduler? = null
    ) = ArrivedViewModel(
        taxiRequestPresentationModel = taxiRequest ?: this.taxiRequestPresentation,
        driversTaxiRequestService = driversTaxiRequestServiceMock,
        taskScheduler = taskScheduler ?: this.taskSchedulerMock,
        context = contextMock
    )
}