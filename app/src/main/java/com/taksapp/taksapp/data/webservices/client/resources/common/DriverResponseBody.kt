package com.taksapp.taksapp.data.webservices.client.resources.common

data class DriverResponseBody(
    val id: String,
    val firstName: String,
    val lastName: String,
    val location: LocationResponseBody?
)