package com.taksapp.taksapp.data.webservices.client

enum class UserType {
    Rider,
    Driver
}

interface SessionStore {
    fun saveAccessToken(accessToken: String)
    fun saveRefreshToken(refreshToken: String)
    fun getAccessToken() : String
    fun getRefreshToken() : String
    fun saveUserType(userType: UserType)
    fun getUserType() : UserType
}