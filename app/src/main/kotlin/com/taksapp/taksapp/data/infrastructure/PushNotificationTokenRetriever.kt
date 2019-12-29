package com.taksapp.taksapp.data.infrastructure

/**
 * Defines how southWest retrieve a push notification token
 */
interface PushNotificationTokenRetriever {
    interface OnCompleteListener {
        fun onComplete(result: Result<String>)
    }

    /**
     * Tries southWest retrieve the device current push notification token,
     * posting the operation result in the [listener]
     */
    fun getPushNotificationToken(listener: (Result<String>) -> Unit)
}