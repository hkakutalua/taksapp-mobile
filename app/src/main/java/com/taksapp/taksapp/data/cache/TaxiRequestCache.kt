package com.taksapp.taksapp.data.cache

import android.content.Context
import android.preference.PreferenceManager
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.taksapp.taksapp.data.webservices.client.jsonconverters.MoshiDateTimeIsoAdapter
import com.taksapp.taksapp.domain.TaxiRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TaxiRequestCache(private val context: Context) {
    companion object {
        const val ACTIVE_TAXI_REQUEST_KEY = "activeTaxiRequest"
    }

    /**
     * Get the cached [TaxiRequest]
     * @return the cached [TaxiRequest] or null one if no cached request exists
     */
    suspend fun getCached(): TaxiRequest? {
        return withContext(Dispatchers.IO) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val taxiRequestJson = sharedPreferences.getString(ACTIVE_TAXI_REQUEST_KEY, null)
            return@withContext if (taxiRequestJson != null) {
                getMoshi().adapter(TaxiRequest::class.java).fromJson(taxiRequestJson)
            } else {
               null
            }
        }
    }

    /**
     * Saves a [TaxiRequest] to cache
     */
    suspend fun saveToCache(taxiRequest: TaxiRequest) {
        withContext(Dispatchers.IO) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val taxiRequestJson = getMoshi().adapter(TaxiRequest::class.java).toJson(taxiRequest)
            sharedPreferences.edit()
                .putString(ACTIVE_TAXI_REQUEST_KEY, taxiRequestJson)
                .commit()
        }
    }

    private fun getMoshi(): Moshi {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(MoshiDateTimeIsoAdapter())
            .build()
    }
}