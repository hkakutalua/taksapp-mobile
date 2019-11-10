package com.taksapp.taksapp.data.webservices.client.httpclients
import java.io.IOException

interface HttpClient {
    /**
     * The remote host in 'https://host:(port)/' format.
     * It must end with '/' character
     */
    val host: String

    /**
     * Sends a GET request to the [url]
     * @return the response of the request
     * @throws [IOException] when a network error or timeout occurrs
     */
    fun get(url: String): HttpResponse

    /**
     * Sends a POST request to the [url] with given JSON [body]
     * @return the response of the request
     * @throws [IOException] when a network error or timeout occurs
     */
    fun post(url: String, body: String): HttpResponse

    /**
     * Sends a POST request to the [url] with given FormData [body]
     * @return the response of the request
     * @throws [IOException] when a network error or timeout occurs
     */
    fun post(url: String, formDataBody: Map<String, String>): HttpResponse
}

