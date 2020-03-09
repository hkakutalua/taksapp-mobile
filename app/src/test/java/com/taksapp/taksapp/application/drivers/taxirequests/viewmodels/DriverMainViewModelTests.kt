package com.taksapp.taksapp.application.drivers.taxirequests.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.events.IncomingTaxiRequestEvent
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.DriversService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.DriversTaxiRequestService.TaxiRequestRetrievalError
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
class DriverMainViewModelTests {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScopeRule = MainCoroutineScopeRule()

    private lateinit var driversServiceMock: DriversService
    private lateinit var driversTaxiRequestService: DriversTaxiRequestService
    private lateinit var devicesServiceMock: DevicesService
    private lateinit var pushNotificationTokenRetriever: PushNotificationTokenRetriever
    private lateinit var taskScheduler: TaskScheduler
    private lateinit var contextMock: Context

    @Before
    fun beforeEachTest() {
        driversServiceMock = mock()
        driversTaxiRequestService = mock()
        devicesServiceMock = mock()
        pushNotificationTokenRetriever = mock()
        taskScheduler = mock()
        contextMock = mock()
    }

    @Test
    fun switchesStatusToOnline() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )
            whenever(driversServiceMock.setAsOnline()).thenReturn(Result.success(null))

            coroutineScopeRule.pauseDispatcher()

            // Act
            driverMainViewModel.switchToOnline()

            // Arrange
            Assert.assertTrue(driverMainViewModel.switchingDriverStatus.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()

            Assert.assertFalse(driverMainViewModel.switchingDriverStatus.getOrAwaitValue())
            Assert.assertTrue(driverMainViewModel.isDriverOnline.getOrAwaitValue())

            verify(driversServiceMock, times(1)).setAsOnline()
        }
    }

    @Test
    fun switchToToOnlineStatusFailsDueToNetworkFailure() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                context = contextMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )

            whenever(driversServiceMock.setAsOnline())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error_message")

            // Act
            driverMainViewModel.switchToOnline()

            // Arrange
            Assert.assertEquals(
                "internet_error_message",
                driverMainViewModel.snackBarErrorEvent.getOrAwaitValue().getContentIfNotHandled())

            Assert.assertFalse(driverMainViewModel.isDriverOnline.getOrAwaitValue())
        }
    }

    @Test
    fun switchToOnlineStatusFailsDueToServerProblems() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                context = contextMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )

            whenever(driversServiceMock.setAsOnline())
                .thenReturn(Result.error(DriversService.OnlineSwitchError.SERVER_ERROR))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error_message")

            // Act
            driverMainViewModel.switchToOnline()

            // Arrange
            Assert.assertEquals(
                "server_error_message",
                driverMainViewModel.snackBarErrorEvent.getOrAwaitValue().getContentIfNotHandled())

            Assert.assertFalse(driverMainViewModel.isDriverOnline.getOrAwaitValue())
        }
    }

    @Test
    fun registersDriverDeviceWhenSwitchToOnlineFails() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                context = contextMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )

            whenever(driversServiceMock.setAsOnline())
                .thenReturn(Result.error(DriversService.OnlineSwitchError.DRIVER_HAS_NO_DEVICE))
                .thenReturn(Result.success(null))
            whenever(devicesServiceMock.registerUserDevice(any(), any()))
                .thenReturn(Result.success(null))
            whenever(pushNotificationTokenRetriever.getPushNotificationToken())
                .thenReturn(Result.success("push_notification_token"))

            // Act
            driverMainViewModel.switchToOnline()

            // Arrange
            inOrder(devicesServiceMock, driversServiceMock) {
                verify(driversServiceMock).setAsOnline()
                verify(devicesServiceMock).registerUserDevice(
                    "push_notification_token", DevicesService.Platform.ANDROID)
                verify(driversServiceMock).setAsOnline()
            }

            Assert.assertTrue(driverMainViewModel.isDriverOnline.getOrAwaitValue())
        }
    }

    @Test
    fun switchToOnlineAgainAfterOnlinePeriodicTaskExecutes() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val driverMainViewModel = buildDriverMainViewModel(
                taskScheduler = taskSchedulerSpy,
                driversService = driversServiceMock
            )

            whenever(driversServiceMock.setAsOnline())
                .thenReturn(Result.success(null))
            whenever(driversServiceMock.setAsOnline())
                .thenReturn(Result.success(null))

            driverMainViewModel.switchToOnline()

            // Act
            taskSchedulerSpy.executePendingTask(DriverMainViewModel.ONLINE_COUNTDOWN_TASK)

            // Assert
            verify(driversServiceMock, times(2)).setAsOnline()
        }
    }

    @Test
    fun switchesStatusToOffline() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )
            whenever(driversServiceMock.setAsOffline()).thenReturn(Result.success(null))

            coroutineScopeRule.pauseDispatcher()

            // Act
            driverMainViewModel.switchToOffline()

            // Arrange
            Assert.assertTrue(driverMainViewModel.switchingDriverStatus.getOrAwaitValue())
            coroutineScopeRule.advanceUntilIdle()

            Assert.assertFalse(driverMainViewModel.switchingDriverStatus.getOrAwaitValue())
            Assert.assertFalse(driverMainViewModel.isDriverOnline.getOrAwaitValue())

            verify(driversServiceMock, times(1)).setAsOffline()
        }
    }

    @Test
    fun switchToToOfflineStatusFailsDueToNetworkFailure() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                context = contextMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )

            whenever(driversServiceMock.setAsOffline())
                .thenThrow(IOException())
            whenever(contextMock.getString(R.string.text_internet_error))
                .thenReturn("internet_error_message")

            // Act
            driverMainViewModel.switchToOffline()

            // Arrange
            Assert.assertEquals(
                "internet_error_message",
                driverMainViewModel.snackBarErrorEvent.getOrAwaitValue().getContentIfNotHandled())
            Assert.assertFalse(driverMainViewModel.isDriverOnline.getOrAwaitValue())
        }
    }

    @Test
    fun switchToOfflineStatusFailsDueToServerProblems() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel(
                driversService = driversServiceMock,
                context = contextMock,
                devicesService = devicesServiceMock,
                pushNotificationTokenRetriever = pushNotificationTokenRetriever
            )

            whenever(driversServiceMock.setAsOffline())
                .thenReturn(Result.error("server_error"))
            whenever(contextMock.getString(R.string.text_server_error))
                .thenReturn("server_error_message")

            // Act
            driverMainViewModel.switchToOffline()

            // Arrange
            Assert.assertEquals(
                "server_error_message",
                driverMainViewModel.snackBarErrorEvent.getOrAwaitValue().getContentIfNotHandled())
            Assert.assertFalse(driverMainViewModel.isDriverOnline.getOrAwaitValue())
        }
    }

    @Test
    fun cancelsOnlinePeriodicTaskWhenStatusIsSwitchedToOffline() {
        coroutineScopeRule.launch {
            // Arrange
            val taskSchedulerSpy = TaskSchedulerSpy()
            val driverMainViewModel = buildDriverMainViewModel(
                taskScheduler = taskSchedulerSpy,
                driversService = driversServiceMock
            )

            whenever(driversServiceMock.setAsOnline())
                .thenReturn(Result.success(null))
            whenever(driversServiceMock.setAsOffline())
                .thenReturn(Result.success(null))

            driverMainViewModel.switchToOnline()

            // Act
            driverMainViewModel.switchToOffline()

            // Assert
            taskSchedulerSpy.assertThatHasCancelledTask(DriverMainViewModel.ONLINE_COUNTDOWN_TASK)
        }
    }

    @Test
    fun showsIncomingTaxiRequestWhenReceivingItByPush() {
        coroutineScopeRule.launch {
            // Arrange
            val incomingTaxiRequest = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().plusSeconds(10))
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val driverMainViewModel = buildDriverMainViewModel()

            whenever(driversTaxiRequestService.getTaxiRequestById("random-taxi-id"))
                .thenReturn(Result.success(incomingTaxiRequest))

            // Act
            driverMainViewModel
                .onTaxiRequestReceived(IncomingTaxiRequestEvent("random-taxi-id"))

            // Assert
            Assert.assertEquals(
                false,
                driverMainViewModel.showIncomingTaxiRequestEvent.value?.hasBeenHandled
            )
        }
    }

    @Test
    fun ignoresTaxiRequestReceivedByPushWhenItsRetrievalFailsDueToInternetError() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel()

            whenever(driversTaxiRequestService.getTaxiRequestById("random-taxi-id"))
                .thenThrow(IOException())

            // Act
            driverMainViewModel
                .onTaxiRequestReceived(IncomingTaxiRequestEvent("random-taxi-id"))

            // Assert
            Assert.assertNull(driverMainViewModel.showIncomingTaxiRequestEvent.value)
        }
    }

    @Test
    fun ignoresTaxiRequestReceivedByPushWhenIsExpired() {
        coroutineScopeRule.launch {
            // Arrange
            val expiredTaxiRequest = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().minusSeconds(2))
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()
            val driverMainViewModel = buildDriverMainViewModel()

            whenever(driversTaxiRequestService.getTaxiRequestById("random-taxi-id"))
                .thenReturn(Result.success(expiredTaxiRequest))

            // Act
            driverMainViewModel
                .onTaxiRequestReceived(IncomingTaxiRequestEvent("random-taxi-id"))

            // Assert
            Assert.assertNull(driverMainViewModel.showIncomingTaxiRequestEvent.value)
        }
    }

    @Test
    fun ignoresTaxiRequestReceivedByPushWhenItIsPastAcceptance() {
        coroutineScopeRule.launch {
            // Arrange
            val taxiRequestPastAcceptance = TaxiRequestFactory.withBuilder()
                .withExpirationDate(DateTime.now().minusSeconds(2))
                .withStatus(Status.ACCEPTED)
                .build()
            val driverMainViewModel = buildDriverMainViewModel()

            whenever(driversTaxiRequestService.getTaxiRequestById("random-taxi-id"))
                .thenReturn(Result.success(taxiRequestPastAcceptance))

            // Act
            driverMainViewModel
                .onTaxiRequestReceived(IncomingTaxiRequestEvent("random-taxi-id"))

            // Assert
            Assert.assertNull(driverMainViewModel.showIncomingTaxiRequestEvent.value)
        }
    }

    @Test
    fun ignoresTaxiRequestReceivedByPushWhenIsNotFound() {
        coroutineScopeRule.launch {
            // Arrange
            val driverMainViewModel = buildDriverMainViewModel()

            whenever(driversTaxiRequestService.getTaxiRequestById("random-taxi-id"))
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))

            // Act
            driverMainViewModel
                .onTaxiRequestReceived(IncomingTaxiRequestEvent("random-taxi-id"))

            // Assert
            Assert.assertNull(driverMainViewModel.showIncomingTaxiRequestEvent.value)
        }
    }

    private fun buildDriverMainViewModel(
        driversService: DriversService? = null,
        driversTaxiRequestService: DriversTaxiRequestService? = null,
        devicesService: DevicesService? = null,
        pushNotificationTokenRetriever: PushNotificationTokenRetriever? = null,
        taskScheduler: TaskScheduler? = null,
        context: Context? = null
    ): DriverMainViewModel {
        return DriverMainViewModel(
            driversService = driversService ?: this.driversServiceMock,
            driversTaxiRequestService = driversTaxiRequestService ?: this.driversTaxiRequestService,
            devicesService = devicesService ?: this.devicesServiceMock,
            pushNotificationTokenRetriever = pushNotificationTokenRetriever
                ?: this.pushNotificationTokenRetriever,
            taskScheduler = taskScheduler ?: this.taskScheduler,
            context = context ?: this.contextMock
        )
    }
}