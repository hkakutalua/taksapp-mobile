package com.taksapp.taksapp.application.taxirequest.viewmodels

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import com.taksapp.taksapp.R
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.CancellationError
import com.taksapp.taksapp.domain.interfaces.FareRepository
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.utils.MainCoroutineScopeRule
import com.taksapp.taksapp.utils.getOrAwaitValue
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
    private lateinit var contextMock: Context
    private lateinit var riderTaxiRequestsRepository: RiderTaxiRequestsRepository
    private lateinit var taxiRequestViewModel: TaxiRequestViewModel

    @Before
    fun beforeEachTest() {
        fareRepositoryMock = mock()
        ridersTaxiRequestServiceMock = mock()
        contextMock = mock()
        riderTaxiRequestsRepository =
            RiderTaxiRequestsRepository(ridersTaxiRequestServiceMock, mock(), mock())
        taxiRequestViewModel = TaxiRequestViewModel(
            TaxiRequest(DateTime.now(), Status.WAITING_ACCEPTANCE),
            riderTaxiRequestsRepository,
            contextMock
        )
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
                taxiRequestViewModel.showTimeoutMessageAndNavigateBackEvent.getOrAwaitValue().hasBeenHandled
            )
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
                taxiRequestViewModel.showTimeoutMessageAndNavigateBackEvent.getOrAwaitValue().hasBeenHandled
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
    fun navigatesToAcceptedStateWhenTaxiRequestIsInAcceptedState() {
        coroutineScope.launch {
            // Act
            val taxiRequestViewModel =
                TaxiRequestViewModel(
                    TaxiRequest(DateTime.now(), Status.ACCEPTED),
                    riderTaxiRequestsRepository,
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
            // Act
            val taxiRequestViewModel =
                TaxiRequestViewModel(
                    TaxiRequest(DateTime.now(), Status.DRIVER_ARRIVED),
                    riderTaxiRequestsRepository,
                    contextMock
                )

            // Assert
            Assert.assertEquals(
                false,
                taxiRequestViewModel.navigateToDriverArrivedStateEvent.value?.hasBeenHandled
            )
        }
    }

//    @Test
//    fun navigatesToMainWhenTaxiRequestExpires() {
//        coroutineScope.launch {
//            // Arrange
//            whenever(ridersTaxiRequestServiceMock.sendTaxiRequest(any(), any()))
//                .thenReturn(Result.success(TaxiRequest(Status.WAITING_ACCEPTANCE)))
//            val origin = LocationPresentationModel(0.28394, 1.02934)
//            val destination = LocationPresentationModel(0.28394, 1.02934)
//
//            // Act
//            taxiRequestViewModel.sendTaxiRequest(origin, destination)
//
//            // Arrange
//        }
//    }
}