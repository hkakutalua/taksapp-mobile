package com.taksapp.taksapp.application.shared.presentationmodels

import com.taksapp.taksapp.application.shared.presentationmodels.LocationPresentationModel

class TaxiRequestPresentationModel(
    val origin: LocationPresentationModel,
    val destination: LocationPresentationModel,
    val originName: String,
    val destinationName: String,
    val driverName: String,
    val driverLocation: LocationPresentationModel?,
    val driverLocationAvailable: Boolean
)
