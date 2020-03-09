package com.taksapp.taksapp.application.riders.taxirequests.presentationmodels

import com.taksapp.taksapp.application.shared.presentationmodels.LocationPresentationModel

data class FareEstimationPresentationModel(
    val northEastBound: LocationPresentationModel,
    val southWestBound: LocationPresentationModel,
    val steps: List<Pair<LocationPresentationModel, LocationPresentationModel>>,
    val fares: List<CompanyPresentationModel>
)