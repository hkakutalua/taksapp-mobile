package com.taksapp.taksapp.data.webservices.client.resources.common

import org.joda.time.DateTime
import java.math.BigDecimal

enum class TripStatus {
    started,
    finished
}

data class TripResponseBody(
    val id: String,
    val status: TripStatus,
    val startDate: DateTime,
    val endDate: DateTime?,
    val fareAmount: BigDecimal,
    val rating: Double?,
    val rider: RiderResponseBody,
    val driver: DriverResponseBody
)