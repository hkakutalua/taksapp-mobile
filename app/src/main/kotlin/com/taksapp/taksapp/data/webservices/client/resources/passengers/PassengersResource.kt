package com.taksapp.taksapp.data.webservices.client.resources.passengers

import com.taksapp.taksapp.data.webservices.client.AuthenticationTokensStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.resources.ApiResource
import com.taksapp.taksapp.data.webservices.client.resources.passengers.requests.PassengerLoginRequest

class PassengersResource(
    override val configurationProvider: ConfigurationProvider,
    private val store: AuthenticationTokensStore) : ApiResource() {

    fun loginRequestBuilder() : PassengerLoginRequest.Builder {
        return PassengerLoginRequest.Builder(configurationProvider, store)
    }
}