package com.taksapp.taksapp.data.webservices

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.TripResponseBodyMapper
import com.taksapp.taksapp.data.webservices.client.resources.common.LocationRequestBody
import com.taksapp.taksapp.data.webservices.client.resources.drivers.DriversResource.CurrentDriverResource.*
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Trip
import com.taksapp.taksapp.domain.interfaces.DriversTripsService
import com.taksapp.taksapp.domain.interfaces.DriversTripsService.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class DriversTripsWebService(private val taksapp: Taksapp) : DriversTripsService {
    override suspend fun startTrip(): Result<Trip, TripStartError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.trips.start()
            return@withContext if (result.successful) {
                Result.success<Trip, TripStartError>(
                    TripResponseBodyMapper.mapToTrip(result.data!!)
                )
            } else {
                when (result.error) {
                    TripStartApiError.NO_TAXI_REQUEST_TO_START_TRIP_FROM ->
                        Result.error(TripStartError.NO_TAXI_REQUEST_TO_START_TRIP_FROM)
                    TripStartApiError.SERVER_ERROR -> Result.error(TripStartError.SERVER_ERROR)
                    else -> Result.error(TripStartError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun getTripById(tripId: String): Result<Trip, TripRetrievalError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.trips.getById(tripId)
            return@withContext if (result.successful) {
                Result.success<Trip, TripRetrievalError>(
                    TripResponseBodyMapper.mapToTrip(result.data!!)
                )
            } else {
                when (result.error) {
                    TripRetrievalApiError.NO_TRIP_FOUND -> Result.error(TripRetrievalError.TRIP_NOT_FOUND)
                    TripRetrievalApiError.SERVER_ERROR -> Result.error(TripRetrievalError.SERVER_ERROR)
                    else -> Result.error(TripRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun getCurrentTrip(): Result<Trip, TripRetrievalError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.trips.getCurrent()
            return@withContext if (result.successful) {
                Result.success<Trip, TripRetrievalError>(
                    TripResponseBodyMapper.mapToTrip(result.data!!)
                )
            } else {
                when (result.error) {
                    TripRetrievalApiError.NO_TRIP_FOUND -> Result.error(TripRetrievalError.TRIP_NOT_FOUND)
                    TripRetrievalApiError.SERVER_ERROR -> Result.error(TripRetrievalError.SERVER_ERROR)
                    else -> Result.error(TripRetrievalError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun addLocationToCurrentTripRoute(locations: List<Location>): Result<Trip, LocationToRouteError> {
        return withContext(Dispatchers.IO) {
            val locationsRequestBody =
                locations.map { l -> LocationRequestBody(l.latitude, l.longitude) }
            val result = taksapp.drivers.me.trips.addToRoute(locationsRequestBody)
            return@withContext if (result.successful) {
                Result.success<Trip, LocationToRouteError>(
                    TripResponseBodyMapper.mapToTrip(result.data!!)
                )
            } else {
                when (result.error) {
                    LocationToRouteApiError.ACTIVE_TRIP_NOT_FOUND ->
                        Result.error(LocationToRouteError.TRIP_NOT_FOUND)
                    LocationToRouteApiError.SERVER_ERROR ->
                        Result.error(LocationToRouteError.SERVER_ERROR)
                    else -> Result.error(LocationToRouteError.SERVER_ERROR)
                }
            }
        }
    }

    override suspend fun finishCurrentTrip(): Result<Trip, TripFinishError> {
        return withContext(Dispatchers.IO) {
            val result = taksapp.drivers.me.trips.finishCurrent()
            return@withContext if (result.successful) {
                Result.success<Trip, TripFinishError>(
                    TripResponseBodyMapper.mapToTrip(result.data!!)
                )
            } else {
                when (result.error) {
                    TripFinishApiError.ACTIVE_TRIP_NOT_FOUND ->
                        Result.error(TripFinishError.TRIP_NOT_FOUND)
                    TripFinishApiError.SERVER_ERROR ->
                        Result.error(TripFinishError.SERVER_ERROR)
                    else -> Result.error(TripFinishError.SERVER_ERROR)
                }
            }
        }
    }
}