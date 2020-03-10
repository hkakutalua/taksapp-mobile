package com.taksapp.taksapp.domain

import org.joda.time.DateTime
import java.io.Serializable

enum class Status {
    WAITING_ACCEPTANCE,
    ACCEPTED,
    DRIVER_ARRIVED,
    CANCELLED,
    FINISHED
}

class TaxiRequest private constructor(
    val id: String,
    val origin: Location,
    val destination: Location,
    val originName: String,
    val destinationName: String,
    val rider: Rider,
    val driver: Driver?,
    val expirationDate: DateTime,
    val status: Status
): Serializable {
    companion object {
        fun withBuilder() = Builder()

        class Builder {
            lateinit var id: String
            lateinit var  origin: Location
            lateinit var  destination: Location
            lateinit var  originName: String
            lateinit var  destinationName: String
            lateinit var  rider: Rider
            var driver: Driver? = null
            lateinit var  expirationDate: DateTime
            lateinit var  status: Status

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

            fun withOptionalDriver(driver: Driver?): Builder {
                this.driver = driver
                return this
            }

            fun withExpirationDate(expirationDate: DateTime): Builder {
                this.expirationDate = expirationDate
                return this
            }

            fun withStatus(status: Status): Builder {
                this.status = status
                return this
            }

            fun build() = TaxiRequest(
                id = id,
                origin = origin,
                destination = destination,
                originName = originName,
                destinationName = destinationName,
                rider = rider,
                driver = driver,
                expirationDate = expirationDate,
                status = status
            )
        }
    }

    fun hasExpired() = DateTime.now() > expirationDate

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TaxiRequest

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}