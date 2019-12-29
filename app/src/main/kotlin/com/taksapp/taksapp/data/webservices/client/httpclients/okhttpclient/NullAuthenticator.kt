package com.taksapp.taksapp.data.webservices.client.httpclients.okhttpclient

import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class NullAuthenticator : Authenticator {
    override fun authenticate(route: Route?, response: Response): Request? = null
}