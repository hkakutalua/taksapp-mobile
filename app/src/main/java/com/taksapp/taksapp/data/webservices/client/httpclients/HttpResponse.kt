package com.taksapp.taksapp.data.webservices.client.httpclients

import java.io.InputStream

class HttpResponse(val code: Int, val body: HttpResponseBody?) {
    class HttpResponseBody(val source: InputStream?) {
        fun string(): String? = source?.bufferedReader()?.readText()
    }

    val isSuccessful get() = code in 200..299
}