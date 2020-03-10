package com.taksapp.taksapp.domain

import org.joda.time.DateTime
import java.math.BigDecimal

enum class TripStatus {
    STARTED,
    FINISHED
}

class Trip(
    val id: String,
    val origin: Location,
    val destination: Location,
    val originLocationName: String,
    val destinationLocationName: String,
    val status: TripStatus,
    val startDate: DateTime,
    val endDate: DateTime?,
    val fareAmount: BigDecimal,
    val rating: Double?,
    val rider: Rider,
    val driver: Driver
) {
    companion object {
        fun withBuilder() = Builder()

        class Builder {
            lateinit var id: String
            lateinit var origin: Location
            lateinit var destination: Location
            lateinit var originName: String
            lateinit var destinationName: String
            lateinit var startDate: DateTime
            var endDate: DateTime? = null
            lateinit var fareAmount: BigDecimal
            var rating: Double? = null
            lateinit var rider: Rider
            lateinit var driver: Driver
            lateinit var status: TripStatus

            fun withId(id: String): Builder {
                this.id = id
                return this
            }

            fun withOrigin(origin: Location, originName: String): Builder {
                this.origin = origin
                this.originName = originName
                return this
            }

            fun withDestination(destination: Location, destinationName: String): Builder {
                this.destination = destination
                this.destinationName = destinationName
                return this
            }

            fun withRider(rider: Rider): Builder {
                this.rider = rider
                return this
            }

            fun withDriver(driver: Driver): Builder {
                this.driver = driver
                return this
            }

            fun withStatus(status: TripStatus): Builder {
                this.status = status
                return this
            }

            fun withStartDate(startDate: DateTime): Builder {
                this.startDate = startDate
                return this
            }

            fun withOptionalEndDate(endDate: DateTime?): Builder {
                this.endDate = endDate
                return this
            }

            fun withFareAmount(amount: BigDecimal): Builder {
                this.fareAmount = amount
                return this
            }

            fun withOptionalRating(rating: Double?): Builder {
                this.rating = rating
                return this
            }

            fun build() = Trip(
                id = id,
                origin = origin,
                destination = destination,
                originLocationName = originName,
                destinationLocationName = destinationName,
                startDate = startDate,
                endDate = endDate,
                fareAmount = fareAmount,
                rating = rating,
                rider = rider,
                driver = driver,
                status = status
            )
        }
    }
}