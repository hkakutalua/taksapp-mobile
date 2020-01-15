package com.taksapp.taksapp.domain

import java.math.BigDecimal

class FareEstimation private constructor(
    val northEastBound: Location,
    val southWestBound: Location,
    val routeSteps: List<LocationPair>,
    val companiesWithFares: Map<Company, Money>) {

    class Builder {
        private var northEastBound: Location = Location(0.0, 0.0)
        private var southWestBound: Location = Location(0.0, 0.0)
        private val routeSteps = mutableListOf<LocationPair>()
        private val companiesWithFares = mutableMapOf<Company, Money>()

        fun northEast(northEastBound: Location): Builder {
            this.northEastBound = northEastBound
            return this
        }

        fun southWest(southWestBound: Location): Builder {
            this.southWestBound = southWestBound
            return this
        }

        fun addStep(start: Location, destination: Location): Builder {
            this.routeSteps.add(LocationPair(start, destination))
            return this
        }

        fun addCompanyWithFare(company: Company, fare: Money): Builder {
            this.companiesWithFares[company] = fare
            return this
        }

        fun build(): FareEstimation = FareEstimation(
            northEastBound,
            southWestBound,
            routeSteps,
            companiesWithFares
        )
    }
}