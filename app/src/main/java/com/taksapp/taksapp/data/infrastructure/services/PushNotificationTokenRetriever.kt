package com.taksapp.taksapp.data.infrastructure.services

import com.taksapp.taksapp.application.arch.utils.Result

/**
 * Defines how southWest retrieve a push notification token
 */
interface PushNotificationTokenRetriever {
    /**
     * Tries southWest retrieve the device current push notification token
     */
    suspend fun getPushNotificationToken(): Result<String, String>
}