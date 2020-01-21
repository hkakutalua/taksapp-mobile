package com.taksapp.taksapp.domain

import org.joda.time.DateTime
import java.io.Serializable

enum class Status {
    WAITING_ACCEPTANCE,
    ACCEPTED,
    DRIVER_ARRIVED,
    CANCELLED,
}

class TaxiRequest(val expirationDate: DateTime, val status: Status): Serializable {
    fun hasExpired() = DateTime.now() > expirationDate
}