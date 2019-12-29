package com.taksapp.taksapp.ui.taxi.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.arch.utils.Event
import com.taksapp.taksapp.domain.FareEstimation
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.interfaces.FareRepository
import com.taksapp.taksapp.ui.taxi.presentationmodels.CompanyPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.FareEstimationPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.PlacePresentationModel

class TaxiRequestViewModel(private val fareRepository: FareRepository) : ViewModel() {
    private val _startLocation = MutableLiveData<PlacePresentationModel>()
    private val _destinationLocation = MutableLiveData<PlacePresentationModel>()
    private val _canFetchFareEstimation = MutableLiveData<Boolean>()
    private val _estimatingFare = MutableLiveData<Boolean>()
    private val _fareEstimationWithRoute = MutableLiveData<FareEstimationPresentationModel>()

    private val _clearDirectionsEvent = MutableLiveData<Event<Nothing>>()
    private val _errorEvent = MutableLiveData<Event<String>>()

    val startLocation: LiveData<String> =
        Transformations.map(_startLocation) { p -> p.primaryText }
    val destinationLocation: LiveData<String> =
        Transformations.map(_destinationLocation) { p -> p.primaryText }
    val canFetchFareEstimation: LiveData<Boolean> = _canFetchFareEstimation
    val estimatingFare: LiveData<Boolean> = _estimatingFare
    val fareEstimationWithRoute: LiveData<FareEstimationPresentationModel> =
        _fareEstimationWithRoute

    val clearDirectionsEvent: LiveData<Event<Nothing>> = _clearDirectionsEvent
    val errorEvent: LiveData<Event<String>> = _errorEvent

    init {
        _startLocation.observeForever { evaluateIfFareCanBeFetched() }
        _destinationLocation.observeForever { evaluateIfFareCanBeFetched() }
        _estimatingFare.observeForever { evaluateIfFareCanBeFetched() }
    }

    fun changeStartLocation(place: PlacePresentationModel) {
        _startLocation.value = place
    }

    fun changeDestinationLocation(place: PlacePresentationModel) {
        _destinationLocation.value = place
    }

    fun fetchFareEstimationWithRoute() {
        if (!canFareBeFetched())
            return

        val start = Location(_startLocation.value!!.latitude, _startLocation.value!!.longitude)
        val destination =
            Location(_destinationLocation.value!!.latitude, _destinationLocation.value!!.longitude)

        val fareEstimationResultLiveData =
            fareRepository.getFareBetweenLocations(start, destination)

        fareEstimationResultLiveData.observeForever { result ->
            if (result.isSuccessful) {
                val fareEstimationWithRoute = mapFareEstimationToPresentationModel(result.data!!)
                _fareEstimationWithRoute.value = fareEstimationWithRoute
            } else if (result.hasFailed) {
                _errorEvent.value = Event(result.error)
            }

            _estimatingFare.value = result.isLoading
        }
    }

    private fun mapFareEstimationToPresentationModel(estimation: FareEstimation): FareEstimationPresentationModel {
        return FareEstimationPresentationModel(
            northEastBound = LocationPresentationModel(
                estimation.northEastBound.latitude,
                estimation.northEastBound.longitude
            ),
            southWestBound = LocationPresentationModel(
                estimation.southWestBound.latitude,
                estimation.southWestBound.longitude
            ),
            steps = estimation.routeSteps.map { locationPair ->
                Pair(
                    LocationPresentationModel(
                        locationPair.start.latitude,
                        locationPair.start.longitude
                    ),
                    LocationPresentationModel(
                        locationPair.start.latitude,
                        locationPair.start.longitude
                    )
                )
            },
            fares = estimation.companiesWithFares.map { entry ->
                val company = entry.component1()
                val fare = entry.component2()
                CompanyPresentationModel(company.id, company.name, company.imageUrl, fare.toString())
            }
        )
    }

    private fun evaluateIfFareCanBeFetched() {
        _canFetchFareEstimation.value = canFareBeFetched()
    }

    private fun canFareBeFetched(): Boolean {
        return _startLocation.value != null &&
                _destinationLocation.value != null &&
                _estimatingFare.value == null || _estimatingFare.value == false
    }

    fun clearDirections() {
        _clearDirectionsEvent.value = Event(null)
    }
}