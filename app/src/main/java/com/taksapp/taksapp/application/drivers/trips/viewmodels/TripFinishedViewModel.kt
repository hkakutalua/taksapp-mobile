package com.taksapp.taksapp.application.drivers.trips.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taksapp.taksapp.application.shared.presentationmodels.TripPresentationModel

class TripFinishedViewModel(tripPresentationModel: TripPresentationModel) : ViewModel() {
    private val _tripPresentation = MutableLiveData<TripPresentationModel>()

    init {
        _tripPresentation.value = tripPresentationModel
    }

    val tripPresentation: LiveData<TripPresentationModel> = _tripPresentation
}