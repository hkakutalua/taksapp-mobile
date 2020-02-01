package com.taksapp.taksapp.background

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import org.greenrobot.eventbus.EventBus

class TaksappFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE_FIELD = "notificationType"
        private const val TAXI_REQUEST_ID_FIELD = "taxiRequestId"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // TODO: Drop messages with sent date superior to 30 seconds

        if (isTaxiRequestStatusChangedMessage(message)) {
            val taxiRequestId = message.data[TAXI_REQUEST_ID_FIELD]!!
            EventBus.getDefault().post(TaxiRequestStatusChangedEvent(taxiRequestId))
        }
    }

    private fun isTaxiRequestStatusChangedMessage(message: RemoteMessage) =
        message.data[NOTIFICATION_TYPE_FIELD] == "taxiRequestStatusChanged"
}