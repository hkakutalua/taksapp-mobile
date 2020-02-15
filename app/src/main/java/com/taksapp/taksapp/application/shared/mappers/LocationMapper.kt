package com.taksapp.taksapp.application.shared.mappers

import com.taksapp.taksapp.application.shared.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.domain.Location

class LocationMapper {
    fun map(location: Location): LocationPresentationModel = LocationPresentationModel(
        location.latitude,
        location.longitude
    )

    fun mapNullable(location: Location?): LocationPresentationModel? {
        return if (location != null) {
            LocationPresentationModel(
                location.latitude,
                location.longitude
            )
        } else null
    }
}