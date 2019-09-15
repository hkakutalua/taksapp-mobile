package com.taksapp.taksapp.data.infrastructure

import android.content.Context
import android.preference.PreferenceManager
import com.taksapp.taksapp.data.webservices.client.AuthenticationTokensStore

class SharedPreferencesTokensStore(val context: Context) : AuthenticationTokensStore {
    companion object {
        private const val accessTokenKey = "PREF_ACCESS_TOKEN"
        private const val refreshTokenKey = "PREF_REFRESH_TOKEN"
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

}