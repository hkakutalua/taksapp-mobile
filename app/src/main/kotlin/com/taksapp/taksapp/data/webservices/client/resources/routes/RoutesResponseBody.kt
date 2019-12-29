package com.taksapp.taksapp.data.webservices.client.resources.routes

class RoutesResponseBody(
    val origin: LocationResponseBody,
    val destination: LocationResponseBody,
    val routes: List<RouteResponseBody>
)

class LegResponseBody(
    val distanceInMeters: Double,
    val durationInSeconds: Double,
    val steps: List<Step>
)

class LocationResponseBody(val latitude: Double, val longitude: Double)

class RouteResponseBody(val bounds: BoundsResponseBody, val legs: List<LegResponseBody>)

class BoundsResponseBody(val northEast: LocationResponseBody, val southWest: LocationResponseBody)

class Step(val origin: LocationResponseBody, val destination: LocationResponseBody)