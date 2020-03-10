package com.taksapp.taksapp.application.drivers.trips.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.application.shared.mappers.TripMapper
import com.taksapp.taksapp.application.shared.presentationmodels.TripPresentationModel
import com.taksapp.taksapp.data.infrastructure.services.TimerTaskScheduler
import com.taksapp.taksapp.domain.TripStatus
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.DriversTripsService
import com.taksapp.taksapp.domain.interfaces.DriversTripsService.TripFinishError
import com.taksapp.taksapp.domain.interfaces.TaskScheduler
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
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
@Suppress("BlockingMethodInNonBlockingContext")
@ExperimentalCoroutinesApi
class TripInProgressViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var driversTripsServiceMock: DriversTripsService
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        driversTripsServiceMock = mock()
        contextMock = mock()
    }

    @Test
    fun finishesTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            val finishedTrip = TripFactory.withBuilder()
                .withId(startedTripPresentation.id)
                .withStatus(TripStatus.FINISHED)
                .build()
            whenever(driversTripsServiceMock.finishCurrentTrip())
                .thenReturn(Result.success(finishedTrip))

            coroutineScopeRule.pauseDispatcher()

            // Act
            viewModel.finishTrip()

            // Assert
            Assert.assertEquals(true, viewModel.processing.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()
            Assert.assertEquals(false, viewModel.processing.getOrAwaitValue())

            Assert.assertEquals(
                startedTripPresentation,
                viewModel.navigateToFinished.getOrAwaitValue().peekContent()
            )

            verify(driversTripsServiceMock, times(1)).finishCurrentTrip()
        }
    }

    @Test
    fun navigatesToFinishedWhenTripFinishingDoesNotFoundActiveTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            whenever(driversTripsServiceMock.finishCurrentTrip())
                .thenReturn(Result.error(TripFinishError.TRIP_NOT_FOUND))

            // Act
            viewModel.finishTrip()

            // Assert
            Assert.assertEquals(false, viewModel.processing.getOrAwaitValue())

            Assert.assertEquals(
                startedTripPresentation,
                viewModel.navigateToFinished.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToFinishTripDueToInternetFailure() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            whenever(driversTripsServiceMock.finishCurrentTrip())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error")

            // Act
            viewModel.finishTrip()

            // Assert
            Assert.assertEquals(
                "internet_error",
                viewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun failsToFinishTripDueToServerIssues() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            whenever(driversTripsServiceMock.finishCurrentTrip())
                .thenReturn(Result.error(TripFinishError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error")

            // Act
            viewModel.finishTrip()

            // Assert
            Assert.assertEquals(
                "server_error",
                viewModel.snackBarErrorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun navigatesToFinishedWhenSynchronizedByPushAsFinished() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            val finishedTrip = TripFactory.withBuilder()
                .withId(startedTripPresentation.id)
                .withStatus(TripStatus.FINISHED)
                .build()
            whenever(driversTripsServiceMock.getTripById(any()))
                .thenReturn(Result.success(finishedTrip))

            // Act
            viewModel.onTripStatusChanged(
                TripStatusChangedEvent(startedTripPresentation.id))

            // Assert
            Assert.assertNotNull(viewModel.navigateToFinished.getOrAwaitValue())

            verify(driversTripsServiceMock, times(1))
                .getTripById(startedTripPresentation.id)
        }
    }

    @Test
    fun doesNotNavigateToFinishedWhenSynchronizedByPushAsNonFinishedTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val viewModel: TripInProgressViewModel = buildViewModel(startedTripPresentation)

            val finishedTrip = TripFactory.withBuilder()
                .withId(startedTripPresentation.id)
                .withStatus(TripStatus.STARTED)
                .build()
            whenever(driversTripsServiceMock.getTripById(any()))
                .thenReturn(Result.success(finishedTrip))

            // Act
            viewModel.onTripStatusChanged(
                TripStatusChangedEvent(startedTripPresentation.id))

            // Assert
            Assert.assertNull(viewModel.navigateToFinished.value)
        }
    }

    @Test
    fun navigatesToFinishedWhenSynchronizedByPullAsFinished() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val taskSchedulerSpy = TaskSchedulerSpy()
            val viewModel: TripInProgressViewModel = buildViewModel(
                startedTripPresentation,
                taskSchedulerSpy
            )

            val finishedTrip = TripFactory.withBuilder()
                .withId(startedTripPresentation.id)
                .withStatus(TripStatus.FINISHED)
                .build()
            whenever(driversTripsServiceMock.getTripById(any()))
                .thenReturn(Result.success(finishedTrip))

            // Act
            taskSchedulerSpy.executePendingTask(TripInProgressViewModel.TRIP_PULL_TASK_ID)

            // Assert
            Assert.assertNotNull(viewModel.navigateToFinished.getOrAwaitValue())

            verify(driversTripsServiceMock, times(1))
                .getTripById(startedTripPresentation.id)
        }
    }

    @Test
    fun doesNotNavigateToFinishedWhenSynchronizedByPullAsNonFinishedTrip() {
        coroutineScopeRule.launch {
            // Arrange
            val startedTripPresentation: TripPresentationModel = buildTripPresentation()
            val taskSchedulerSpy = TaskSchedulerSpy()
            val viewModel: TripInProgressViewModel = buildViewModel(
                startedTripPresentation,
                taskSchedulerSpy
            )

            val finishedTrip = TripFactory.withBuilder()
                .withId(startedTripPresentation.id)
                .withStatus(TripStatus.STARTED)
                .build()
            whenever(driversTripsServiceMock.getTripById(any()))
                .thenReturn(Result.success(finishedTrip))

            // Act
            taskSchedulerSpy.executePendingTask(TripInProgressViewModel.TRIP_PULL_TASK_ID)

            // Assert
            Assert.assertNull(viewModel.navigateToFinished.value)
        }
    }

    private fun buildViewModel(
        tripPresentation: TripPresentationModel,
        taskScheduler: TaskScheduler? = null): TripInProgressViewModel {
        return TripInProgressViewModel(
            tripPresentation = tripPresentation,
            driversTripsService = driversTripsServiceMock,
            taskScheduler = taskScheduler ?: TimerTaskScheduler(),
            context = contextMock
        )
    }

    private fun buildTripPresentation(): TripPresentationModel {
        return TripMapper().map(TripFactory.withBuilder().build())
    }
}