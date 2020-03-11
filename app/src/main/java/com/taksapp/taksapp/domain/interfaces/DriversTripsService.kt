package com.taksapp.taksapp.domain.interfaces

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Trip
import java.io.IOException

interface DriversTripsService {
    enum class TripStartError {
        NO_TAXI_REQUEST_TO_START_TRIP_FROM,
        SERVER_ERROR
    }

    enum class TripRetrievalError {
        TRIP_NOT_FOUND,
        SERVER_ERROR
    }

    enum class LocationToRouteError {
        TRIP_NOT_FOUND,
        SERVER_ERROR
    }

    enum class TripFinishError {
        TRIP_NOT_FOUND,
        SERVER_ERROR
    }

    /**
     * Start a trip
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun startTrip(): Result<Trip, TripStartError>

    /**
     * Get driver's trip by it's [tripId]
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun getTripById(tripId: String): Result<Trip, TripRetrievalError>

    /**
     * Get driver's current trip
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun getCurrentTrip(): Result<Trip, TripRetrievalError>

    /**
     * Add location to current trip route
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun addLocationToCurrentTripRoute(locations: List<Location>): Result<Trip, LocationToRouteError>

    /**
     * Finishes the current driver's trip
     * @return an [Result] indicating if the operation was successful
     * @throws [IOException] when a network error or timeout occurs
     */
    @Throws(IOException::class)
    suspend fun finishCurrentTrip(): Result<Trip, TripFinishError>
}