package com.taksapp.taksapp.data.webservices.client.resources.routes

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response

class RoutesResource(private val configurationProvider: ConfigurationProvider) {
    fun getBetween(
        origin: LocationQueryParameter,
        destination: LocationQueryParameter
    ): Response<RoutesResponseBody, String> {
        val httpClient = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        val response = httpClient.get(
            "api/v1/routes?" +
                    "origin=${origin.latitude},${origin.longitude}&" +
                    "destination=${destination.latitude},${destination.longitude}"
        )

        return if (response.isSuccessful) {
            val body = jsonConverter.fromJson(
                response.body!!.source!!,
                RoutesResponseBody::class
            )

            Response.success(body)
        } else {
            val error = response.body?.string() ?: ""
            Response.failure(error)
        }
    }
}