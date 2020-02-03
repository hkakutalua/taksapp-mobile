package com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient

import com.taksapp.taksapp.data.webservices.client.httpclients.HttpClient
import com.taksapp.taksapp.data.webservices.client.httpclients.HttpResponse
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@ExperimentalTime
class OkHttpClientAdapter(
    override val host: String,
    timeout: Duration,
    tokenRefreshAuthenticator: Authenticator,
    accessTokenInterceptor: Interceptor
) : HttpClient {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(10, unit = TimeUnit.SECONDS)
        .writeTimeout(timeout.toLongMilliseconds(), unit = TimeUnit.MILLISECONDS)
        .readTimeout(timeout.toLongMilliseconds(), unit = TimeUnit.MILLISECONDS)
        .authenticator(tokenRefreshAuthenticator)
        .addInterceptor(accessTokenInterceptor)
        .build()

    init {
        require(host.isNotBlank())
        require(host.endsWith("/"))
    }

    override fun get(url: String): HttpResponse {
        val request = Request.Builder()
            .url("$host$url")
            .get()
            .build()

        val response = httpClient.newCall(request).execute()

        return HttpResponse(
            code = response.code,
            body = HttpResponse.HttpResponseBody(
                response.body?.byteStream()
            )
        )
    }

    override fun post(url: String, body: String): HttpResponse {
        val request = Request.Builder()
            .url("$host$url")
            .post(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()

        return HttpResponse(
            code = response.code,
            body = HttpResponse.HttpResponseBody(
                response.body?.byteStream()
            )
        )
    }

    override fun post(url: String, formDataBody: Map<String, String>): HttpResponse {
        val bodyBuilder = FormBody.Builder()

        for (entry in formDataBody.entries) {
            bodyBuilder.add(entry.key, entry.value)
        }

        val request = Request.Builder()
            .url("$host$url")
            .post(bodyBuilder.build())
            .build()
        val response = httpClient.newCall(request).execute()

        return HttpResponse(
            code = response.code,
            body = HttpResponse.HttpResponseBody(response.body?.byteStream())
        )
    }

    override fun put(url: String, body: String): HttpResponse {
        val request = Request.Builder()
            .url("$host$url")
            .put(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        val response = httpClient.newCall(request).execute()

        return HttpResponse(
            response.code,
            body = HttpResponse.HttpResponseBody(response.body?.byteStream())
        )
    }

    override fun patch(url: String, body: String?): HttpResponse {
        val requestBuilder = Request.Builder().url("$host$url")

        if (body != null) {
            requestBuilder.patch(body.toRequestBody("application/json; charset=utf-8".toMediaType()))
        } else {
            requestBuilder.patch("".toRequestBody("application/json; charset=utf-8".toMediaType()))
        }

        val response = httpClient.newCall(requestBuilder.build()).execute()

        return HttpResponse(
            response.code,
            body = HttpResponse.HttpResponseBody(response.body?.byteStream())
        )
    }
}