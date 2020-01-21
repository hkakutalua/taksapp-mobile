package com.taksapp.taksapp.ui.taxi.viewmodels

import android.content.Context
import androidx.lifecycle.*
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Event
import com.taksapp.taksapp.data.repositories.CreateTaxiRequestError
import com.taksapp.taksapp.data.repositories.RiderTaxiRequestsRepository
import com.taksapp.taksapp.domain.FareEstimation
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.interfaces.FareRepository
import com.taksapp.taksapp.ui.taxi.presentationmodels.CompanyPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.FareEstimationPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.LocationPresentationModel
import com.taksapp.taksapp.ui.taxi.presentationmodels.PlacePresentationModel
import kotlinx.coroutines.launch
import java.io.IOException

class FareEstimationViewModel(
    private val fareRepository: FareRepository,
    private val riderTaxiRequestsRepository: RiderTaxiRequestsRepository,
    private val context: Context
) : ViewModel() {
    private val _startLocation = MutableLiveData<PlacePresentationModel>()
    private val _destinationLocation = MutableLiveData<PlacePresentationModel>()
    private val _canFetchFareEstimation = MutableLiveData<Boolean>()
    private val _estimatingFare = MutableLiveData<Boolean>()
    private val _fareEstimationWithRoute = MutableLiveData<FareEstimationPresentationModel>()
    private val _sendingTaxiRequest = MutableLiveData<Boolean>()
    private val _navigateToTaxiRequestEvent = MutableLiveData<Event<TaxiRequest>>()
    private val _clearDirectionsEvent = MutableLiveData<Event<Nothing>>()
    private val _errorEvent = MutableLiveData<Event<String>>()

    val startLocationName: LiveData<String> =
        Transformations.map(_startLocation) { p -> p.primaryText }
    val destinationLocationName: LiveData<String> =
        Transformations.map(_destinationLocation) { p -> p.primaryText }
    val canFetchFareEstimation: LiveData<Boolean> = _canFetchFareEstimation
    val estimatingFare: LiveData<Boolean> = _estimatingFare
    val fareEstimationWithRoute: LiveData<FareEstimationPresentationModel> =
        _fareEstimationWithRoute
    val sendingTaxiRequest: LiveData<Boolean> = _sendingTaxiRequest
    val navigateToTaxiRequestEvent: LiveData<Event<TaxiRequest>> = _navigateToTaxiRequestEvent
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

    fun sendTaxiRequest() {
        if (_startLocation.value == null || _destinationLocation.value == null)
            return

        _sendingTaxiRequest.value = true

        val originLocation =
            Location(_startLocation.value!!.latitude, _startLocation.value!!.longitude)
        val destinationLocation =
            Location(_destinationLocation.value!!.latitude, _destinationLocation.value!!.longitude)

        viewModelScope.launch {
            try {
                val result = riderTaxiRequestsRepository.create(originLocation, destinationLocation)

                if (result.isSuccessful) {
                    val taxiRequest = result.data
                    _navigateToTaxiRequestEvent.postValue(Event(taxiRequest))
                } else if (result.hasFailed) {
                    var errorEvent = Event("")
                    when (result.error) {
                        CreateTaxiRequestError.NO_AVAILABLE_DRIVERS ->
                            errorEvent = Event(context.getString(R.string.error_no_available_drivers))
                        CreateTaxiRequestError.SERVER_ERROR ->
                            errorEvent = Event(context.getString(R.string.text_server_error))
                    }
                    _errorEvent.postValue(errorEvent)
                }
            } catch (e: IOException) {
                _errorEvent.postValue(Event(context.getString(R.string.text_internet_error)))
            } finally {
                _sendingTaxiRequest.value = false
            }
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
                CompanyPresentationModel(
                    company.id,
                    company.name,
                    company.imageUrl,
                    fare.toString()
                )
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