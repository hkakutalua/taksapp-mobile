package com.taksapp.taksapp.data.webservices.client.resources.riders

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider

class RidersResource(provider: ConfigurationProvider) {
    val taxiRequests = RidersTaxiRequestsResource(provider)
}