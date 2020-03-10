package com.taksapp.taksapp.data.webservices.client.resources.common

import org.joda.time.DateTime

enum class TaxiRequestStatus {
    waitingAcceptance,
    accepted,
    driverArrived,
    cancelled,
    finished
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