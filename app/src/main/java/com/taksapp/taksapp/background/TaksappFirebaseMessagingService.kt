package com.taksapp.taksapp.background

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.taksapp.taksapp.application.taxirequest.backgroundhandlers.TaxiRequestBackgroundHandler
import org.koin.android.ext.android.inject

class TaksappFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val NOTIFICATION_TYPE_FIELD = "notificationType"
        private const val TAXI_REQUEST_ID_FIELD = "taxiRequestId"
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        if (isTaxiRequestStatusChangedMessage(message)) {
            val taxiRequestId = message.data[TAXI_REQUEST_ID_FIELD]
            taxiRequestId?.let {
                val taxiRequestBackgroundHandler: TaxiRequestBackgroundHandler by inject()
                taxiRequestBackgroundHandler.handleTaxiRequestStatusChanged(taxiRequestId)
            }
        }
    }

    private fun isTaxiRequestStatusChangedMessage(message: RemoteMessage) =
        message.data[NOTIFICATION_TYPE_FIELD] == "taxiRequestStatusChanged"
}