package com.taksapp.taksapp.data.webservices.client.httpclients
import java.io.IOException

interface HttpClient {
    /**
     * The base URL for the client. It must end with '/' character
     */
    val baseUrl: String

    /**
     * Sends a POST request to the [url] with given JSON [body]
     * @return the response of the request
     * @throws [IOException] when a network error or timeout occurs
     */
    fun post(url: String, body: String): HttpResponse
}

