package com.taksapp.taksapp.data.webservices.client

import com.taksapp.taksapp.data.webservices.client.resources.common.*
import com.taksapp.taksapp.data.webservices.client.resources.common.TripStatus
import com.taksapp.taksapp.domain.*

class TripResponseBodyMapper {
    companion object {
        fun mapToTrip(tripResponseBody: TripResponseBody): Trip {
            return Trip(
                id = tripResponseBody.id,
                status = mapTripStatus(tripResponseBody.status),
                startDate = tripResponseBody.startDate,
                endDate = tripResponseBody.endDate,
                fareAmount = tripResponseBody.fareAmount,
                rating = tripResponseBody.rating,
                rider = mapToRider(tripResponseBody.rider),
                driver = mapToDriver(tripResponseBody.driver)
            )
        }

        private fun mapToRider(rider: RiderResponseBody) =
            Rider(rider.id, rider.firstName, rider.lastName, mapToNullableLocation(rider.location))

        private fun mapToDriver(driver: DriverResponseBody) =
            Driver(driver.id, driver.firstName, driver.lastName, mapToNullableLocation(driver.location))

        private fun mapToNullableLocation(sourceLocation: LocationResponseBody?) =
            if (sourceLocation != null)
                Location(sourceLocation.latitude, sourceLocation.longitude)
            else null

        private fun mapTripStatus(tripStatus: TripStatus): com.taksapp.taksapp.domain.TripStatus {
            return when (tripStatus) {
                TripStatus.started -> com.taksapp.taksapp.domain.TripStatus.STARTED
                TripStatus.finished ->com.taksapp.taksapp.domain.TripStatus.FINISHED
            }
        }
    }
}