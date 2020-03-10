package com.taksapp.taksapp.background

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taksapp.taksapp.domain.events.IncomingTaxiRequestEvent
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.events.TripStatusChangedEvent
import org.greenrobot.eventbus.EventBus

class TaksappFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE_FIELD = "notificationType"
        private const val TAXI_REQUEST_ID_FIELD = "taxiRequestId"
        private const val TRIP_ID_FIELD = "tripId"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // TODO: Drop messages with sent date superior to 30 seconds

        when {
            isTaxiRequestStatusChangedMessage(message) -> {
                val taxiRequestId = message.data[TAXI_REQUEST_ID_FIELD]!!
                EventBus.getDefault().post(TaxiRequestStatusChangedEvent(taxiRequestId))
            }

            isIncomingTaxiRequestMessage(message) -> {
                val taxiRequestId = message.data[TAXI_REQUEST_ID_FIELD]!!
                EventBus.getDefault().post(IncomingTaxiRequestEvent(taxiRequestId))
            }

            isTripStatusChangedMessage(message) -> {
                val tripId = message.data[TRIP_ID_FIELD]!!
                EventBus.getDefault().post(TripStatusChangedEvent(tripId))
            }
        }
    }

    private fun isIncomingTaxiRequestMessage(message: RemoteMessage) =
        message.data[NOTIFICATION_TYPE_FIELD] == "incomingTaxiRequest"

    private fun isTaxiRequestStatusChangedMessage(message: RemoteMessage) =
        message.data[NOTIFICATION_TYPE_FIELD] == "taxiRequestStatusChanged"

    private fun isTripStatusChangedMessage(message: RemoteMessage) =
        message.data[NOTIFICATION_TYPE_FIELD] == "tripStatusChanged"
}