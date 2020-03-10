package com.taksapp.taksapp.utils.factories

import com.taksapp.taksapp.domain.*
import org.joda.time.DateTime
import java.math.BigDecimal
import java.util.*

class TripFactory {
    companion object {
        fun withBuilder(): Builder = Builder()
    }

    class Builder {
        private var id: String? = null
        private var status = TripStatus.STARTED

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withStatus(status: TripStatus): Builder {
            this.status = status
            return this
        }

        fun build(): Trip {
            return Trip.withBuilder()
                .withId(id ?: UUID.randomUUID().toString())
                .withOrigin(Location(0.0, 0.0), "Foo")
                .withDestination(Location(0.0, 0.0), "Bar")
                .withRider(Rider("", "Henrick", "Kakutalua", Location(0.0, 0.0)))
                .withDriver(Driver("", "Henrick", "Kakutalua", Location(0.0, 0.0)))
                .withStartDate(DateTime.now())
                .withStatus(status)
                .withFareAmount(BigDecimal(1000.00))
                .build()
        }
    }
}