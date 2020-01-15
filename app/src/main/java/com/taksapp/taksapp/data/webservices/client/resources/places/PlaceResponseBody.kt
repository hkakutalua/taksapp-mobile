package com.taksapp.taksapp.data.webservices.client.resources.places

data class Location(val latitude: Double, val longitude: Double)

data class PlaceResponseBody(
    val id: String,
    val primaryText: String,
    val secondaryText: String,
    val location: Location
)