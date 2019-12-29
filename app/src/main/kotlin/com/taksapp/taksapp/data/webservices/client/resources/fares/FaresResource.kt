package com.taksapp.taksapp.data.webservices.client.resources.fares

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.resources.common.PageResponseBody

class FaresResource(private val configurationProvider: ConfigurationProvider) {
    fun get(
        distanceInMeters: Double,
        durationInSeconds: Double
    ): Response<PageResponseBody<FareResponseBody>, String> {
        val httpClient = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        val response = httpClient.get("api/v1/fares?" +
                "distance=${distanceInMeters}&" +
                "duration=${durationInSeconds}&" +
                "page=1&count=30")

        return if (response.isSuccessful) {
            val body = jsonConverter.fromJson(
                response.body!!.source!!,
                classOf<PageResponseBody<FareResponseBody>>(),
                PageResponseBody::class.java,
                FareResponseBody::class.java
            )

            Response.success(body)
        } else {
            val error = response.body?.string() ?: ""
            Response.failure(error)
        }
    }

    private inline fun <reified T: Any> classOf() = T::class
}