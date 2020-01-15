package com.taksapp.taksapp.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.exceptions.ConnectionErrorException
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Place
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PlacesRepository(private val taksapp: Taksapp, private val context: Context) {
    fun autocompleteSearch(input: String): LiveData<Result<List<Place>, String>> {
        val result = MutableLiveData<Result<List<Place>, String>>()

        GlobalScope.launch {
            try {
                val response = taksapp.places.autocompleteSearch(input)
                if (response.successful) {
                    val placesPageResponseBody = response.data
                    val places = placesPageResponseBody?.data?.map { p ->
                        Place(
                            p.primaryText,
                            p.secondaryText,
                            Location(p.location.latitude, p.location.longitude))
                    } ?: listOf()
                    result.postValue(Result.success(places))
                } else {
                    result.postValue(Result.error(response.error))
                }
            } catch (ex: ConnectionErrorException) {
                result.postValue(Result.error(context.getString(R.string.text_internet_error)))
            }
        }

        result.value = Result.loading()
        return result
    }
}