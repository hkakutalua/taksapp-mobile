package com.taksapp.taksapp.application.riders.taxirequests.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.infrastructure.services.PushNotificationTokenRetriever
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.interfaces.DevicesService
import com.taksapp.taksapp.domain.interfaces.FareRepository
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestError
import com.taksapp.taksapp.application.riders.taxirequests.presentationmodels.PlacePresentationModel
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import com.taksapp.taksapp.utils.getOrAwaitValue
import junitparams.JUnitParamsRunner
import junitparams.Parameters
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException



@ExperimentalCoroutinesApi
@RunWith(JUnitParamsRunner::class)
class FareEstimationViewModelTests {
    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()
    @get:Rule
    val coroutineScope = MainCoroutineScopeRule()

    private lateinit var fareRepositoryMock: FareRepository
    private lateinit var ridersTaxiRequestServiceMock: RidersTaxiRequestService
    private lateinit var devicesServiceMock: DevicesService
    private lateinit var pushNotificationTokenRetrieverMock: PushNotificationTokenRetriever
    private lateinit var contextMock: Context
    private lateinit var fareEstimationViewModel: FareEstimationViewModel

    @Before
    fun beforeEachTest() {
        ridersTaxiRequestServiceMock = mock()
        fareRepositoryMock = mock()
        devicesServiceMock = mock()
        pushNotificationTokenRetrieverMock = mock()
        contextMock = mock()
        val taxiRequestsRepository =
            RiderTaxiRequestsRepository(
                ridersTaxiRequestServiceMock,
                devicesServiceMock,
                pushNotificationTokenRetrieverMock
            )

        fareEstimationViewModel =
            FareEstimationViewModel(
                fareRepositoryMock, taxiRequestsRepository, contextMock
            )
    }

    @Test
    fun sendsTaxiRequest() {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.success(taxiRequest))
            val originPlace =
                PlacePresentationModel(
                    "Luanda", "", 0.28394, 1.02934
                )
            val destinationPlace =
                PlacePresentationModel(
                    "Benguela", "", 0.123, 1.0945
                )
            fareEstimationViewModel.changeStartLocation(originPlace)
            fareEstimationViewModel.changeDestinationLocation(destinationPlace)
            coroutineScope.pauseDispatcher()

            // Act
            fareEstimationViewModel.sendTaxiRequest()

            // Assert
            Assert.assertEquals(true, fareEstimationViewModel.sendingTaxiRequest.value)

            coroutineScope.advanceUntilIdle()
            Assert.assertEquals(false, fareEstimationViewModel.sendingTaxiRequest.value)
            Assert.assertEquals(
                false,
                fareEstimationViewModel.navigateToTaxiRequestEvent.getOrAwaitValue().hasBeenHandled
            )

            verify(ridersTaxiRequestServiceMock)
                .sendTaxiRequest(Location(0.28394, 1.02934), Location(0.123, 1.0945))
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
            val originPlace =
                PlacePresentationModel(
                    "Luanda", "", 0.28394, 1.02934
                )
            val destinationPlace =
                PlacePresentationModel(
                    "Benguela", "", 0.123, 1.0945
                )
            fareEstimationViewModel.changeStartLocation(originPlace)
            fareEstimationViewModel.changeDestinationLocation(destinationPlace)

            // Act
            fareEstimationViewModel.sendTaxiRequest()

            // Assert
            Assert.assertEquals(false, fareEstimationViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                "internet_error",
                fareEstimationViewModel.errorEvent.getOrAwaitValue().peekContent()
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
            val originPlace =
                PlacePresentationModel(
                    "Luanda", "", 0.28394, 1.02934
                )
            val destinationPlace =
                PlacePresentationModel(
                    "Benguela", "", 0.123, 1.0945
                )
            fareEstimationViewModel.changeStartLocation(originPlace)
            fareEstimationViewModel.changeDestinationLocation(destinationPlace)

            // Act
            fareEstimationViewModel.sendTaxiRequest()

            // Assert
            Assert.assertEquals(false, fareEstimationViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                "no_available_drivers",
                fareEstimationViewModel.errorEvent.getOrAwaitValue().peekContent()
            )
        }
    }

    @Test
    fun registersDriverDeviceWhenTaxiRequestFails() {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.DEVICE_NOT_REGISTERED))
                .thenReturn(Result.success(taxiRequest))
            whenever(pushNotificationTokenRetrieverMock.getPushNotificationToken())
                .thenReturn(Result.success("push-notification-token"))
            whenever(devicesServiceMock.registerUserDevice(any(), any()))
                .thenReturn(Result.success(null))

            val originPlace =
                PlacePresentationModel(
                    "Luanda", "", 0.28394, 1.02934
                )
            val destinationPlace =
                PlacePresentationModel(
                    "Benguela", "", 0.28394, 1.02934
                )
            fareEstimationViewModel.changeStartLocation(originPlace)
            fareEstimationViewModel.changeDestinationLocation(destinationPlace)

            // Act
            fareEstimationViewModel.sendTaxiRequest()

            // Assert
            Assert.assertEquals(false, fareEstimationViewModel.sendingTaxiRequest.getOrAwaitValue())
            Assert.assertEquals(
                false,
                fareEstimationViewModel.navigateToTaxiRequestEvent.getOrAwaitValue().hasBeenHandled
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


    private fun parametersForNavigatesToTaxiRequestDueToActiveTaxiRequest(): Array<Status> {
        return arrayOf(Status.ACCEPTED, Status.DRIVER_ARRIVED, Status.WAITING_ACCEPTANCE)
    }

    @Test
    @Parameters
    fun navigatesToTaxiRequestDueToActiveTaxiRequest(activeStatus: Status) {
        coroutineScope.launch {
            // Arrange
            val taxiRequest = TaxiRequestFactory.withBuilder()
                .withStatus(activeStatus).build()

            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
                .thenReturn(Result.error(TaxiRequestError.ACTIVE_TAXI_REQUEST_EXISTS))
            whenever(ridersTaxiRequestServiceMock.getCurrentTaxiRequest())
                .thenReturn(Result.success(taxiRequest))

            val originPlace =
                PlacePresentationModel(
                    "Luanda", "", 0.28394, 1.02934
                )
            val destinationPlace =
                PlacePresentationModel(
                    "Benguela", "", 0.123, 1.0945
                )
            fareEstimationViewModel.changeStartLocation(originPlace)
            fareEstimationViewModel.changeDestinationLocation(destinationPlace)

            // Act
            fareEstimationViewModel.sendTaxiRequest()

            // Assert
            Assert.assertEquals(
                false,
                fareEstimationViewModel.navigateToTaxiRequestEvent.getOrAwaitValue().hasBeenHandled
            )
            verify(ridersTaxiRequestServiceMock).getCurrentTaxiRequest()
        }
    }
}