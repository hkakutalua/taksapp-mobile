package com.taksapp.taksapp.data.webservices.client

interface AuthenticationTokensStore {
    fun saveAccessToken(accessToken: String)
    fun saveRefreshToken(refreshToken: String)
    fun getAccessToken() : String
    fun getRefreshToken() : String
}