package com.taksapp.taksapp.ui.taxi.presentationmodels

data class FareEstimationPresentationModel(
    val northEastBound: LocationPresentationModel,
    val southWestBound: LocationPresentationModel,
    val steps: List<Pair<LocationPresentationModel, LocationPresentationModel>>,
    val fares: List<CompanyPresentationModel>
)