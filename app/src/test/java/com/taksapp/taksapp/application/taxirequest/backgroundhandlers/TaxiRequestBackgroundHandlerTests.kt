package com.taksapp.taksapp.application.taxirequest.backgroundhandlers

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.background.TaksappFirebaseMessagingService
import com.taksapp.taksapp.data.cache.TaxiRequestCache
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestRetrievalError
import com.taksapp.taksapp.utils.factories.TaxiRequestFactory
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTest
import org.koin.test.mock.declare
import java.util.*

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TaxiRequestBackgroundHandlerTests : KoinTest {
    private lateinit var taxiRequestCache: TaxiRequestCache
    private lateinit var taxiRequestStatusEventHandler: TaxiRequestStatusEventHandler
    private lateinit var taxiRequestService: RidersTaxiRequestService
    private val context: Context = ApplicationProvider.getApplicationContext()

    @Before
    fun beforeEachTest() {
        taxiRequestCache = TaxiRequestCache(context)
        taxiRequestStatusEventHandler = spy()
        taxiRequestService = mock()

        declare {
            factory { taxiRequestService }
        }
    }

    open class TaxiRequestStatusEventHandler {
        @Subscribe
        open fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {}
    }

    @Test
    fun dispatchesTaxiRequestWhenTheresNoCachedOne() {
        runBlocking {
            // Arrange
            val remoteMessage = RemoteMessage.Builder("push-notification-token")
                .setMessageId("bogus-id")
                .addData("notificationType", "taxiRequestStatusChanged")
                .addData("taxiRequestId", "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a")
                .build()
            val firebaseMessagingService = TaksappFirebaseMessagingService()

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(UUID.randomUUID().toString())
                .withStatus(Status.ACCEPTED).build()

            whenever(taxiRequestService.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))

            EventBus.getDefault().register(taxiRequestStatusEventHandler)

            // Act
            firebaseMessagingService.onMessageReceived(remoteMessage)

            // Assert
            verify(taxiRequestStatusEventHandler)
                .onTaxiRequestStatusChanged(argThat { taxiRequest.status == Status.ACCEPTED })

            val savedTaxiRequest = taxiRequestCache.getCached()
            Assert.assertEquals(Status.ACCEPTED, savedTaxiRequest?.status)
        }
    }

    @Test
    fun dispatchesTaxiRequestWhenStatusOrIdChangesComparedWithCachedOne() {
        runBlocking {
            // Arrange
            val remoteMessage = RemoteMessage.Builder("push-notification-token")
                .setMessageId("bogus-id")
                .addData("notificationType", "taxiRequestStatusChanged")
                .addData("taxiRequestId", "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a")
                .build()
            val firebaseMessagingService = TaksappFirebaseMessagingService()

            val acceptedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(UUID.randomUUID().toString())
                .withStatus(Status.ACCEPTED).build()
            val cachedTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(UUID.randomUUID().toString())
                .withStatus(Status.WAITING_ACCEPTANCE).build()

            whenever(taxiRequestService.getCurrentTaxiRequest())
                .thenReturn(Result.success(acceptedTaxiRequest))
            taxiRequestCache.saveToCache(cachedTaxiRequest)

            EventBus.getDefault().register(taxiRequestStatusEventHandler)

            // Act
            firebaseMessagingService.onMessageReceived(remoteMessage)

            // Assert
            verify(taxiRequestStatusEventHandler)
                .onTaxiRequestStatusChanged(argThat { taxiRequest.status == Status.ACCEPTED })

            val savedTaxiRequest = taxiRequestCache.getCached()
            Assert.assertEquals(Status.ACCEPTED, savedTaxiRequest?.status)
        }
    }

    @Test
    fun doesNotDispatchTaxiRequestWhenStatusAndTaxiRequestAreTheSame() {
        runBlocking {
            // Arrange
            val remoteMessage = RemoteMessage.Builder("push-notification-token")
                .setMessageId("bogus-id")
                .addData("notificationType", "taxiRequestStatusChanged")
                .addData("taxiRequestId", "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a")
                .build()
            val firebaseMessagingService = TaksappFirebaseMessagingService()

            val sameTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(UUID.randomUUID().toString())
                .withStatus(Status.WAITING_ACCEPTANCE)
                .build()

            whenever(taxiRequestService.getCurrentTaxiRequest())
                .thenReturn(Result.success(sameTaxiRequest))
            taxiRequestCache.saveToCache(sameTaxiRequest)

            EventBus.getDefault().register(taxiRequestStatusEventHandler)

            // Act
            firebaseMessagingService.onMessageReceived(remoteMessage)

            // Assert
            verify(taxiRequestStatusEventHandler, never()).onTaxiRequestStatusChanged(any())
        }
    }

    @Test
    fun dispatchesCancelledRequestWhenStatusIsCancelled() {
        runBlocking {
            // Arrange
            val cancelledTaxiRequestId = "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a"

            val remoteMessage = RemoteMessage.Builder("push-notification-token")
                .setMessageId("bogus-id")
                .addData("notificationType", "taxiRequestStatusChanged")
                .addData("taxiRequestId", cancelledTaxiRequestId)
                .build()
            val firebaseMessagingService = TaksappFirebaseMessagingService()

            val cancelledTaxiRequest = TaxiRequestFactory.withBuilder()
                .withId(cancelledTaxiRequestId)
                .withStatus(Status.CANCELLED).build()

            whenever(taxiRequestService.getCurrentTaxiRequest())
                .thenReturn(Result.error(TaxiRequestRetrievalError.NOT_FOUND))
            whenever(taxiRequestService.getTaxiRequestById(cancelledTaxiRequestId))
                .thenReturn(Result.success(cancelledTaxiRequest))

            EventBus.getDefault().register(taxiRequestStatusEventHandler)

            // Act
            firebaseMessagingService.onMessageReceived(remoteMessage)

            // Assert
            inOrder(taxiRequestService) {
                verify(taxiRequestService).getCurrentTaxiRequest()
                verify(taxiRequestService).getTaxiRequestById(cancelledTaxiRequestId)
            }

            verify(taxiRequestStatusEventHandler)
                .onTaxiRequestStatusChanged(argThat { taxiRequest.status == Status.CANCELLED })

            val savedTaxiRequest = taxiRequestCache.getCached()
            Assert.assertEquals(Status.CANCELLED, savedTaxiRequest?.status)
        }
    }
}