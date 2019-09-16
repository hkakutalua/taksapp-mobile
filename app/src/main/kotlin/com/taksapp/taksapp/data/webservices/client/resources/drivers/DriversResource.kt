package com.taksapp.taksapp.data.webservices.client.resources.drivers

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.resources.ApiResource
import com.taksapp.taksapp.data.webservices.client.resources.common.requests.LoginRequest
import com.taksapp.taksapp.data.webservices.client.resources.drivers.requests.DriverLoginRequest

class DriversResource(
    override val configurationProvider: ConfigurationProvider,
    private val store: SessionStore) : ApiResource() {

    fun loginRequestBuilder() : LoginRequest.BaseBuilder {
        return DriverLoginRequest.Builder(configurationProvider, store)
    }
}