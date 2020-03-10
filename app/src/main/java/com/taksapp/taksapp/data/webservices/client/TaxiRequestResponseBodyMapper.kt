package com.taksapp.taksapp.data.webservices.client

import com.taksapp.taksapp.data.webservices.client.resources.common.*
import com.taksapp.taksapp.domain.*

class TaxiRequestResponseBodyMapper {
    companion object {
        fun mapToTaxiRequest(taxiRequestResponseBody: TaxiRequestResponseBody): TaxiRequest {
            return TaxiRequest.withBuilder()
                .withId(taxiRequestResponseBody.id)
                .withOrigin(
                    mapToLocation(taxiRequestResponseBody.origin),
                    taxiRequestResponseBody.originLocationName
                )
                .withDestination(
                    mapToLocation(taxiRequestResponseBody.destination),
                    taxiRequestResponseBody.destinationLocationName
                )
                .withRider(mapToRider(taxiRequestResponseBody.rider))
                .withOptionalDriver(mapToNullableDriver(taxiRequestResponseBody.driver))
                .withExpirationDate(taxiRequestResponseBody.expirationDate)
                .withStatus(mapTaxiRequestStatus(taxiRequestResponseBody.status))
                .build()
        }

        private fun mapToRider(rider: RiderResponseBody) =
            Rider(rider.id, rider.firstName, rider.lastName, mapToNullableLocation(rider.location))

        private fun mapToNullableDriver(driver: DriverResponseBody?) =
            if (driver != null)
                Driver(
                    driver.id,
                    driver.firstName,
                    driver.lastName,
                    mapToNullableLocation(driver.location)
                )
            else null

        private fun mapToLocation(sourceLocation: LocationResponseBody) =
            Location(sourceLocation.latitude, sourceLocation.longitude)

        private fun mapToNullableLocation(sourceLocation: LocationResponseBody?) =
            if (sourceLocation != null)
                Location(sourceLocation.latitude, sourceLocation.longitude)
            else null

        private fun mapTaxiRequestStatus(taxiRequestStatus: TaxiRequestStatus): Status {
            return when (taxiRequestStatus) {
                TaxiRequestStatus.waitingAcceptance -> Status.WAITING_ACCEPTANCE
                TaxiRequestStatus.accepted -> Status.ACCEPTED
                TaxiRequestStatus.driverArrived -> Status.DRIVER_ARRIVED
                TaxiRequestStatus.cancelled -> Status.CANCELLED
                TaxiRequestStatus.finished -> Status.FINISHED
            }
        }
    }
}