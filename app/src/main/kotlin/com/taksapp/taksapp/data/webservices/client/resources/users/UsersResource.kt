package com.taksapp.taksapp.data.webservices.client.resources.users

import com.taksapp.taksapp.data.webservices.client.ConfigurationProvider
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.LoginRequest
import com.taksapp.taksapp.data.webservices.client.resources.users.requests.SignUpRequest

class UsersResource(
    private val configurationProvider: ConfigurationProvider,
    private val store: SessionStore
) {
    fun loginRequestBuilder(): LoginRequest.Builder {
        return LoginRequest.Builder(configurationProvider, store)
    }

    fun signUpRequestBuilder(): SignUpRequest.Builder {
        return SignUpRequest.Builder(configurationProvider)
    }
}