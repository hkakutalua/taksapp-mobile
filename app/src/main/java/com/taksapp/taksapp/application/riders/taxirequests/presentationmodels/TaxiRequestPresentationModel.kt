package com.taksapp.taksapp.application.riders.taxirequests.presentationmodels

class TaxiRequestPresentationModel(
    val origin: LocationPresentationModel,
    val destination: LocationPresentationModel,
    val originName: String,
    val destinationName: String,
    val driverName: String,
    val driverLocation: LocationPresentationModel?,
    val driverLocationAvailable: Boolean
)
