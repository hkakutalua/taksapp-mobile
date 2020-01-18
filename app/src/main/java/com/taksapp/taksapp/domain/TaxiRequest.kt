package com.taksapp.taksapp.domain

enum class Status {
    WAITING_ACCEPTANCE,
    ACCEPTED,
    DRIVER_ARRIVED,
    CANCELLED,
}

class TaxiRequest(val status: Status);