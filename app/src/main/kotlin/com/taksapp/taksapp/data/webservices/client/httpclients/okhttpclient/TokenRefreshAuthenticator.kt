package com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient

import com.taksapp.taksapp.BuildConfig
import com.taksapp.taksapp.data.webservices.client.SessionExpiryListener
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.jsonconverters.JsonConverter
import okhttp3.*
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

data class TokenRefreshResponseBody(val access_token: String, val refresh_token: String)

/**
 * An authenticator that challenges a failed authentication by refreshing the access token
 */
@ExperimentalTime
class TokenRefreshAuthenticator(
    private val sessionStore: SessionStore,
    private val sessionExpiryListener: SessionExpiryListener,
    private val jsonConverter: JsonConverter) : Authenticator {
    private var challengeTries = 0

    override fun authenticate(route: Route?, response: Response): Request? {
        if (shouldGiveUpAuthentication()) {
            sessionExpiryListener.onSessionExpired()
            return null
        }

        val refreshToken = sessionStore.getRefreshToken()
        if (refreshToken.isBlank()) {
            sessionExpiryListener.onSessionExpired()
            return null
        }


        val httpClient = OkHttpClientAdapter(
            BuildConfig.BASE_URL,
            30.toDuration(TimeUnit.SECONDS),
            NullAuthenticator(),
            NullInterceptor()
        )

        val body = mapOf(
            "grant_type" to "refresh_token",
            "refresh_token" to sessionStore.getRefreshToken(),
            "client_id" to "mobile_application"
        )

        val httpResponse = httpClient.post("token", body)
        if (httpResponse.isSuccessful) {
            val tokenRefreshResponseBody =
                jsonConverter.fromJson(httpResponse.body!!.source!!, TokenRefreshResponseBody::class)
            sessionStore.saveAccessToken(tokenRefreshResponseBody.access_token)
            sessionStore.saveRefreshToken(tokenRefreshResponseBody.refresh_token)

            return response.request.newBuilder()
                .header("Authorization", "Bearer ${tokenRefreshResponseBody.access_token}")
                .build()
        }

        sessionExpiryListener.onSessionExpired()
        return null
    }

    private fun shouldGiveUpAuthentication(): Boolean {
        if (challengeTries > 0) {
            challengeTries = 0
            return true
        }

        return false
    }
}