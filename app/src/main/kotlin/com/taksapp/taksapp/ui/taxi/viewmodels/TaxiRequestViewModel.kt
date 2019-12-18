package com.taksapp.taksapp.ui.taxi.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.PlacePresentationModel

class TaxiRequestViewModel : ViewModel() {
    private val _startLocation = MutableLiveData<PlacePresentationModel>()
    private val _destinationLocation = MutableLiveData<PlacePresentationModel>()
    private val _canFetchFareEstimation = MutableLiveData<Boolean>()
    private val _estimatingFare = MutableLiveData<Boolean>()

    val startLocation: LiveData<String> =
        Transformations.map(_startLocation) { p -> p.primaryText }
    val destinationLocation: LiveData<String> =
        Transformations.map(_destinationLocation) { p -> p.primaryText }
    val canFetchFareEstimation: LiveData<Boolean> = _canFetchFareEstimation
    val estimatingFare: LiveData<Boolean> = _estimatingFare

    init {
        _startLocation.observeForever { evaluateIfFareCanBeFetched() }
        _destinationLocation.observeForever { evaluateIfFareCanBeFetched() }
    }

    fun changeStartLocation(place: PlacePresentationModel) {
        _startLocation.value = place
    }

    fun changeDestinationLocation(place: PlacePresentationModel) {
        _destinationLocation.value = place
    }

    private fun evaluateIfFareCanBeFetched() {
        val canFareBeFetched = _startLocation.value != null &&
                _destinationLocation.value != null
        _canFetchFareEstimation.value = canFareBeFetched
    }
}