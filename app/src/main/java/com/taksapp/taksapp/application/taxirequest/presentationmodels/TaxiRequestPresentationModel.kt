package com.taksapp.taksapp.application.taxirequest.presentationmodels

class TaxiRequestPresentationModel(
    val origin: LocationPresentationModel,
    val destination: LocationPresentationModel,
    val originName: String,
    val destinationName: String,
    val driverName: String
)
