package com.taksapp.taksapp.data.webservices.client.resources.places

import com.taksapp.taksapp.data.webservices.client.resources.common.LocationResponseBody

data class PlaceResponseBody(
    val id: String,
    val primaryText: String,
    val secondaryText: String,
    val location: LocationResponseBody
)