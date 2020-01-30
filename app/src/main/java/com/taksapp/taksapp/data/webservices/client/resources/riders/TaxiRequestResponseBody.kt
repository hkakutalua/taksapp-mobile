package com.taksapp.taksapp.data.webservices.client.resources.riders

import com.taksapp.taksapp.data.webservices.client.resources.common.LocationResponseBody
import org.joda.time.DateTime

enum class TaxiRequestStatus {
    waitingAcceptance,
    accepted,
    driverArrived,
    cancelled,
}

data class TaxiRequestResponseBody(
    val id: String,
    val origin: LocationResponseBody,
    val destination: LocationResponseBody,
    val originLocationName: String,
    val destinationLocationName: String,
    val rider: RiderResponseBody,
    val driver: DriverResponseBody?,
    val expirationDate: DateTime,
    val status: TaxiRequestStatus
)

data class RiderResponseBody(
    val id: String,
    val firstName: String,
    val lastName: String,
    val location: LocationResponseBody?
)

data class DriverResponseBody(
    val id: String,
    val firstName: String,
    val lastName: String,
    val location: LocationResponseBody?
)