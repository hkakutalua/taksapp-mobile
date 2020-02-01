package com.taksapp.taksapp.background

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.messaging.RemoteMessage
import com.nhaarman.mockitokotlin2.argThat
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaksappFirebaseMessagingServiceTests {
    private val taxiRequestEventHandlerMock: TaxiRequestEventHandler = mock()

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
            .onEventReceived(argThat { taxiRequestId == "28c9a3cc-34a7-4ffa-a811-fbb1f2fd0f7a" })
    }

    open class TaxiRequestEventHandler {
        @Subscribe
        open fun onEventReceived(event: TaxiRequestStatusChangedEvent) {}
    }
}