package com.taksapp.taksapp.application.riders.taxirequests.viewmodels

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.application.arch.utils.Event
import com.taksapp.taksapp.data.repositories.PlacesRepository
import com.taksapp.taksapp.application.riders.taxirequests.presentationmodels.PlacePresentationModel
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask
import kotlin.time.ExperimentalTime
import kotlin.time.toDuration

@ExperimentalTime
class AutocompletePlaceChooserViewModel(
    private val placesRepository: PlacesRepository) : ViewModel() {
    private val _loading = MutableLiveData<Boolean>()
    private val _places = MutableLiveData<List<PlacePresentationModel>>()
    private val _error = MutableLiveData<Event<String>>()
    private var autocompleteTask: TimerTask? = null

    val query = MutableLiveData<String>()
    val loading: LiveData<Boolean> = _loading
    val places: LiveData<List<PlacePresentationModel>> = _places
    val error: LiveData<Event<String>> = _error

    init {
        query.observeForever {
            autocompleteTask?.cancel()
            autocompleteTask = timerTask {
                val handler = Handler(Looper.getMainLooper())
                handler.post { doAutocompleteSearch() }
            }
            Timer().schedule(autocompleteTask, 2.toDuration(TimeUnit.SECONDS).toLongMilliseconds())
        }
    }

    private fun doAutocompleteSearch() {
        if (query.value.isNullOrBlank())
            return

        placesRepository.autocompleteSearch(query.value!!).observeForever { result ->
            if (result.isSuccessful) {
                val places = result.data

                _places.value = places?.map { p ->
                    PlacePresentationModel(
                        primaryText = p.primaryText,
                        secondaryText = p.secondaryText,
                        latitude = p.location.latitude,
                        longitude = p.location.longitude
                    )
                } ?: listOf()

            } else if (result.hasFailed) {
                _error.value = Event(result.error ?: "")
            }

            _loading.value = result.isLoading
        }
    }
}