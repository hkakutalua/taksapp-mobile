package com.taksapp.taksapp.data.webservices.client.resources.riders

import com.taksapp.taksapp.data.webservices.client.resources.common.Location
import org.joda.time.DateTime

enum class TaxiRequestStatus {
    waitingAcceptance,
    accepted,
    driverArrived,
    cancelled,
}

data class TaxiRequestResponseBody(
    val id: String,
    val origin: Location,
    val destination: Location,
    val expirationDate: DateTime,
    val status: TaxiRequestStatus
)