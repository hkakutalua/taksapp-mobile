package com.taksapp.taksapp.background

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.*
import com.taksapp.taksapp.domain.events.IncomingTaxiRequestEvent
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaksappFirebaseMessagingServiceTests {
    private val taxiRequestEventHandlerMock: TaxiRequestEventHandler = mock()
    private val tripEventHandlerMock: TripEventHandler = mock()

    @Test
    fun dispatchesTaxiRequestEventAfterReceivingMessage() {
        // Arrange
        val remoteMessage = RemoteMessage.Builder("push-notification-token")
            .setMessageId("bogus-id")
            .addData("notificationType", "taxiRequestStatusChanged")
            .addData("taxiRequestId", "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a")
            .build()
        val firebaseMessagingService = TaksappFirebaseMessagingService()

        EventBus
            .getDefault()
            .register(taxiRequestEventHandlerMock)

        // Act
        firebaseMessagingService.onMessageReceived(remoteMessage)

        // Assert
        verify(taxiRequestEventHandlerMock)
            .onTaxiRequestStatusChanged(argThat { taxiRequestId == "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a" })
    }

    @Test
    fun dispatchesIncomingTaxiRequestEventAfterReceivingMessage() {
        // Arrange
        val remoteMessage = RemoteMessage.Builder("push-notification-token")
            .setMessageId("bogus-id")
            .addData("notificationType", "incomingTaxiRequest")
            .addData("taxiRequestId", "4c8151d8-3702-4543-aef7-bfd3d0996b96")
            .build()
        val firebaseMessagingService = TaksappFirebaseMessagingService()

        EventBus
            .getDefault()
            .register(taxiRequestEventHandlerMock)

        // Act
        firebaseMessagingService.onMessageReceived(remoteMessage)

        // Assert
        verify(taxiRequestEventHandlerMock)
            .onIncomingTaxiRequest(argThat { taxiRequestId == "4c8151d8-3702-4543-aef7-bfd3d0996b96" })
    }

    @Test
    fun dispatchesTripStatusEventAfterReceivingMessage() {
        // Arrange
        val remoteMessage = RemoteMessage.Builder("push-notification-token")
            .setMessageId("bogus-id")
            .addData("notificationType", "tripStatusChanged")
            .addData("tripId", "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a")
            .build()
        val firebaseMessagingService = TaksappFirebaseMessagingService()

        EventBus
            .getDefault()
            .register(tripEventHandlerMock)

        // Act
        firebaseMessagingService.onMessageReceived(remoteMessage)

        // Assert
        verify(tripEventHandlerMock)
            .onTripStatusChanged(argThat { tripId == "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a" })
    }

    open class TaxiRequestEventHandler {
        @Subscribe
        open fun onTaxiRequestStatusChanged(event: TaxiRequestStatusChangedEvent) {}

        @Subscribe
        open fun onIncomingTaxiRequest(event: IncomingTaxiRequestEvent) {}
    }

    open class TripEventHandler {
        @Subscribe
        open fun onTripStatusChanged(event: TripStatusChangedEvent) {}
    }
}