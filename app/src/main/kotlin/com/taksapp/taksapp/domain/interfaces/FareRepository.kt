package com.taksapp.taksapp.domain.interfaces

import androidx.lifecycle.LiveData
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.domain.FareEstimation
import com.taksapp.taksapp.domain.Location

interface FareRepository {
    fun getFareBetweenLocations(
        start: Location,
        destination: Location
    ): LiveData<Result<FareEstimation, String>>
}