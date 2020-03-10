package com.taksapp.taksapp.domain

import org.joda.time.DateTime
import java.math.BigDecimal

enum class TripStatus {
    STARTED,
    FINISHED
}

class Trip(
    val id: String,
    val status: TripStatus,
    val startDate: DateTime,
    val endDate: DateTime?,
    val fareAmount: BigDecimal,
    val rating: Double?,
    val rider: Rider,
    val driver: Driver
)