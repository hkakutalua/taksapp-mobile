package com.taksapp.taksapp.domain

data class Location(val latitude: Double, val longitude: Double)

data class Place(
    val primaryText: String,
    val secondaryText: String,
    val location: Location
)