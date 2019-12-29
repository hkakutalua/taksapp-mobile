package com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient

import com.taksapp.taksapp.data.webservices.client.SessionStore
import okhttp3.Interceptor
import okhttp3.Response

/**
 * An interceptor that injects the logged in user access token in all requests
 */
class AccessTokenInterceptor(private val sessionStore: SessionStore) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val rebuiltHeaders = request.headers.newBuilder()
            .add("Authorization", "Bearer ${sessionStore.getAccessToken()}")
            .build()

        val rebuiltRequest = request.newBuilder()
            .headers(rebuiltHeaders)
            .build()

        return chain.proceed(rebuiltRequest)
    }
}