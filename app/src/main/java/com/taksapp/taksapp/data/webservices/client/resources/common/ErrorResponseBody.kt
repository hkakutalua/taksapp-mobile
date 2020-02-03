package com.taksapp.taksapp.data.webservices.client.resources.common

class Error(val source: String, val code: String)

class ErrorResponseBody(val status: Int, val errors: List<Error>)