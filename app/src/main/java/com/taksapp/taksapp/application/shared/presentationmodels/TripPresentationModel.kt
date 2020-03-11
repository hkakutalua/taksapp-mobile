package com.taksapp.taksapp.application.shared.presentationmodels

import java.io.Serializable

class TripPresentationModel(
    val id: String,
    val origin: LocationPresentationModel,
    val destination: LocationPresentationModel,
    val originName: String,
    val destinationName: String,
    val driverName: String,
    val driverLocation: LocationPresentationModel?,
    val driverLocationAvailable: Boolean,
    val riderName: String,
    val formattedFareAmount: String
): Serializable
