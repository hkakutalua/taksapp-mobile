package com.taksapp.taksapp.data.infrastructure

/**
 * Defines how to retrieve a push notification token
 */
interface PushNotificationTokenRetriever {
    interface OnCompleteListener {
        fun onComplete(result: Result<String>)
    }

    /**
     * Tries to retrieve the device current push notification token,
     * posting the operation result in the [listener]
     */
    fun getPushNotificationToken(listener: (Result<String>) -> Unit)
}