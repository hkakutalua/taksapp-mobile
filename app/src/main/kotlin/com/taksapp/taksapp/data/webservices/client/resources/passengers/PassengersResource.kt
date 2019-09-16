package com.taksapp.taksapp.data.webservices.client.resources.passengers

import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.resources.ApiResource
import com.taksapp.taksapp.data.webservices.client.resources.common.requests.LoginRequest
import com.taksapp.taksapp.data.webservices.client.resources.passengers.requests.PassengerLoginRequest

class PassengersResource(
    override val configurationProvider: ConfigurationProvider,
    private val store: SessionStore) : ApiResource() {

    fun loginRequestBuilder() : LoginRequest.BaseBuilder {
        return PassengerLoginRequest.Builder(configurationProvider, store)
    }
}