package com.taksapp.taksapp.data.repositories

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.taksapp.taksapp.R
import com.taksapp.taksapp.arch.utils.Result
import com.taksapp.taksapp.data.webservices.client.Response
import com.taksapp.taksapp.data.webservices.client.Taksapp
import com.taksapp.taksapp.data.webservices.client.resources.common.PageResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.fares.FareResponseBody
import com.taksapp.taksapp.data.webservices.client.resources.routes.LocationQueryParameter
import com.taksapp.taksapp.data.webservices.client.resources.routes.RoutesResponseBody
import com.taksapp.taksapp.domain.Company
import com.taksapp.taksapp.domain.FareEstimation
import com.taksapp.taksapp.domain.Location
import com.taksapp.taksapp.domain.Money
import com.taksapp.taksapp.domain.interfaces.FareRepository
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.*

class RemoteFareRepository(
    private val taksapp: Taksapp,
    private val context: Context
) : FareRepository {

    override fun getFareBetweenLocations(
        start: Location,
        destination: Location
    ): LiveData<Result<FareEstimation, String>> {
        val result = MutableLiveData<Result<FareEstimation, String>>()

        GlobalScope.launch {
            val startParameter = LocationQueryParameter(start.latitude, start.longitude)
            val destinationParameter =
                LocationQueryParameter(destination.latitude, destination.longitude)

            try {
                val routeResponse = taksapp.routes.getBetween(startParameter, destinationParameter)
                var fareResponse = Response.failure<PageResponseBody<FareResponseBody>, String>("")
                if (routeResponse.successful) {
                    val legBody = routeResponse.data!!.routes[0].legs[0]
                    fareResponse = taksapp.fares.get(
                        distanceInMeters = legBody.distanceInMeters,
                        durationInSeconds = legBody.durationInSeconds
                    )
                }

                if (routeResponse.successful && fareResponse.successful) {
                    val fareEstimation =
                        buildFareEstimation(routeResponse.data!!, fareResponse.data!!.data)
                    result.postValue(Result.success(fareEstimation))
                } else {
                    result.postValue(Result.error(routeResponse.error ?: fareResponse.error ?: ""))
                }

            } catch (ex: java.io.IOException) {
                result.postValue(Result.error(context.getString(R.string.text_internet_error)))
            }
        }

        result.value = Result.loading()
        return result
    }

    private fun buildFareEstimation(
        routesBody: RoutesResponseBody,
        fares: List<FareResponseBody>
    ): FareEstimation {
        val routeBounds = routesBody.routes[0].bounds
        val fareEstimationBuilder = FareEstimation.Builder()
            .northEast(Location(routeBounds.northEast.latitude, routeBounds.northEast.longitude))
            .southWest(Location(routeBounds.southWest.latitude, routeBounds.southWest.longitude))

        for (step in routesBody.routes[0].legs[0].steps) {
            fareEstimationBuilder.addStep(
                start = Location(step.origin.latitude, step.origin.longitude),
                destination = Location(step.destination.latitude, step.destination.longitude)
            )
        }

        for (fare in fares) {
            val company = Company(fare.company.id, fare.company.name, fare.company.image)
            fareEstimationBuilder.addCompanyWithFare(
                company,
                Money(Currency.getInstance(fare.currency), BigDecimal.valueOf(fare.amount))
            )
        }

        return fareEstimationBuilder.build()
    }
}