package com.taksapp.taksapp.data.webservices.client.resources.drivers.requests

import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.UserType
import com.taksapp.taksapp.data.webservices.client.resources.common.requests.LoginRequest

class DriverLoginRequest(
        email: String,
        password: String,
        pushNotificationToken: String,
        configurationProvider: ConfigurationProvider,
        store: SessionStore)
    : LoginRequest(email, password, pushNotificationToken, configurationProvider, store) {

    class Builder(
        private val configurationProvider: ConfigurationProvider,
        private val store: SessionStore)
        : BaseBuilder() {

        override fun build(): LoginRequest {
            return DriverLoginRequest(
                email = this.email,
                password = this.password,
                pushNotificationToken = this.pushNotificationToken,
                configurationProvider = configurationProvider,
                store = store
            )
        }
    }

    override val endpoint: String
        get() = "api/v1/drivers/login"

    override fun saveUserType() = store.saveUserType(UserType.DRIVER)
}