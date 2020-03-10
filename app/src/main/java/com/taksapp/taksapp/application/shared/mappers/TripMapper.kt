package com.taksapp.taksapp.application.shared.mappers

import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.application.shared.presentationmodels.TripPresentationModel
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.Trip

class TripMapper {
    fun map(trip: Trip) =
        TripPresentationModel(
            id = trip.id,
            origin = LocationMapper().map(trip.origin),
            destination = LocationMapper().map(trip.destination),
            originName = trip.originLocationName,
            destinationName = trip.destinationLocationName,
            driverName = "${trip.driver.firstName} ${trip.driver.lastName}",
            driverLocation = LocationMapper().mapNullable(trip.driver.location),
            driverLocationAvailable = trip.driver.location != null,
            riderName = "${trip.rider.firstName} ${trip.rider.lastName}"
        )
}