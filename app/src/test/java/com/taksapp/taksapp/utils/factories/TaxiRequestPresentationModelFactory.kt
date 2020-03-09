package com.taksapp.taksapp.utils.factories

import com.taksapp.taksapp.application.shared.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel

class TaxiRequestPresentationModelFactory {
    companion object {
        fun withBuilder(): Builder = Builder()
    }

    class Builder {
        fun build() = TaxiRequestPresentationModel(
            id = "",
            origin = LocationPresentationModel(0.0, 0.0),
            destination = LocationPresentationModel(0.0, 0.0),
            originName = "",
            destinationName = "",
            driverName = "",
            driverLocation = LocationPresentationModel(0.0, 0.0),
            driverLocationAvailable = true,
            riderName = ""
        )
    }
}