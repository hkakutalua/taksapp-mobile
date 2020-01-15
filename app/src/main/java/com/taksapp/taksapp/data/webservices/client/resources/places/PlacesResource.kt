package com.taksapp.taksapp.data.webservices.client.resources.places

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.exceptions.ConnectionErrorException
import com.taksapp.taksapp.data.webservices.client.resources.common.PageResponseBody
import java.io.IOException

class PlacesResource(private val configurationProvider: ConfigurationProvider) {
    fun autocompleteSearch(query: String): Response<PageResponseBody<PlaceResponseBody>, String> {
        val httpClient = configurationProvider.client
        val jsonConverter = configurationProvider.jsonConverter

        try {
            val response = httpClient.get("api/v1/places/autocomplete?query='${query}'")
            return if (response.isSuccessful) {
                val body = jsonConverter.fromJson(
                    response.body!!.source!!,
                    classOf<PageResponseBody<PlaceResponseBody>>(),
                    PageResponseBody::class.java,
                    PlaceResponseBody::class.java
                )

                Response.success(body)
            } else {
                val error = response.body?.string() ?: ""
                Response.failure(error)
            }
        } catch (e: IOException) {
            throw ConnectionErrorException(e.localizedMessage)
        }
    }

    private inline fun <reified T: Any> classOf() = T::class
}