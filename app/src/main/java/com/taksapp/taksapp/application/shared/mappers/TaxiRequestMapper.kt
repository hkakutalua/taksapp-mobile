package com.taksapp.taksapp.application.shared.mappers

import com.taksapp.taksapp.application.shared.presentationmodels.TaxiRequestPresentationModel
import com.taksapp.taksapp.domain.TaxiRequest

class TaxiRequestMapper {
    fun map(taxiRequest: TaxiRequest) =
        TaxiRequestPresentationModel(
            id = taxiRequest.id,
            origin = LocationMapper().map(taxiRequest.origin),
            destination = LocationMapper().map(taxiRequest.destination),
            originName = taxiRequest.originName,
            destinationName = taxiRequest.destinationName,
            driverName = "${taxiRequest.driver?.firstName} ${taxiRequest.driver?.lastName}",
            driverLocation = LocationMapper().mapNullable(taxiRequest.driver?.location),
            driverLocationAvailable = taxiRequest.driver?.location != null,
            riderName = "${taxiRequest.rider.firstName} ${taxiRequest.rider.lastName}"
        )
}