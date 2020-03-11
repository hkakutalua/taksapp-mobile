package com.taksapp.taksapp.utils.factories

import com.taksapp.taksapp.domain.*
import org.joda.time.DateTime
import org.joda.time.Duration
import java.util.*

class TaxiRequestFactory {
    companion object {
        fun withBuilder(): Builder = Builder()
    }

    class Builder {
        private var id: String? = null
        private var status = Status.WAITING_ACCEPTANCE
        private var expirationDate: DateTime? = null

        fun withId(id: String): Builder {
            this.id = id
            return this
        }

        fun withStatus(status: Status): Builder {
            this.status = status
            return this
        }

        fun withExpirationDate(expirationDate: DateTime): Builder {
            this.expirationDate = expirationDate
            return this
        }

        fun build(): TaxiRequest {
            return TaxiRequest.withBuilder()
                .withId(id ?: UUID.randomUUID().toString())
                .withOrigin(Location(0.0, 0.0), "Foo")
                .withDestination(Location(0.0, 0.0), "Bar")
                .withRider(Rider("", "Henrick", "Kakutalua", Location(0.0, 0.0)))
                .withOptionalDriver(
                    if (status != Status.WAITING_ACCEPTANCE)
                        Driver("", "Henrick", "Kakutalua", Location(0.0, 0.0))
                    else null)
                .withExpirationDate(expirationDate ?:
                    DateTime.now().plus(Duration.standardSeconds(30)))
                .withStatus(status)
                .build()
        }
    }
}