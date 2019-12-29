package com.taksapp.taksapp.data.infrastructure

import android.content.Context
import android.preference.PreferenceManager
import com.taksapp.taksapp.data.webservices.client.SessionStore
import com.taksapp.taksapp.data.webservices.client.UserType

class SharedPreferencesSessionStore(private val context: Context) : SessionStore {
    companion object {
        private const val accessTokenKey = "PREF_ACCESS_TOKEN"
        private const val refreshTokenKey = "PREF_REFRESH_TOKEN"
        private const val userTypeKey = "PREF_USER_TYPE"
    }

    override fun saveAccessToken(accessToken: String) {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()

        editor.putString(accessTokenKey, accessToken)
        editor.apply()
    }

    override fun saveRefreshToken(refreshToken: String) {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()

        editor.putString(refreshTokenKey, refreshToken)
        editor.apply()
    }

    override fun getAccessToken(): String {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(accessTokenKey, "")!!
    }

    override fun getRefreshToken(): String {
        return PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(refreshTokenKey, "")!!
    }

    override fun saveUserType(userType: UserType) {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()

        editor.putString(userTypeKey, userTypeToString(userType))
        editor.apply()

    }

    override fun getUserType(): UserType {
        val userType = PreferenceManager
            .getDefaultSharedPreferences(context)
            .getString(userTypeKey, "")!!

        return userTypeFromString(userType)
    }

    override fun clearAll() {
        val editor = PreferenceManager
            .getDefaultSharedPreferences(context)
            .edit()

        editor.remove(accessTokenKey)
        editor.remove(refreshTokenKey)
        editor.remove(userTypeKey)
        editor.apply()
    }

    private fun userTypeToString(userType: UserType): String {
        return when (userType) {
            UserType.DRIVER -> "driver"
            UserType.RIDER -> "RIDER"
        }
    }

    private fun userTypeFromString(userType: String): UserType {
        return when (userType) {
            "driver" -> UserType.DRIVER
            "RIDER" -> UserType.RIDER
            else -> throw Exception("Unknown user type '$userType'")
        }
    }
}