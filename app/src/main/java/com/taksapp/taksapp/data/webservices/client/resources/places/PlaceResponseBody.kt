package com.taksapp.taksapp.data.webservices.client.resources.places

import com.taksapp.taksapp.data.webservices.client.resources.common.Location

data class PlaceResponseBody(
    val id: String,
    val primaryText: String,
    val secondaryText: String,
    val location: Location
)