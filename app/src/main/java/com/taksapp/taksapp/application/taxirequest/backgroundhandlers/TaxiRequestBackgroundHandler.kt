package com.taksapp.taksapp.application.taxirequest.backgroundhandlers

import com.taksapp.taksapp.application.arch.utils.Result
import com.taksapp.taksapp.data.cache.TaxiRequestCache
import com.taksapp.taksapp.domain.Status
import com.taksapp.taksapp.domain.TaxiRequest
import com.taksapp.taksapp.domain.events.TaxiRequestStatusChangedEvent
import com.taksapp.taksapp.domain.interfaces.RidersTaxiRequestService
import com.taksapp.taksapp.domain.interfaces.TaxiRequestRetrievalError
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.greenrobot.eventbus.EventBus

class TaxiRequestBackgroundHandler(
    private val taxiRequestService: RidersTaxiRequestService,
    private val taxiRequestCache: TaxiRequestCache
) {
    fun handleTaxiRequestStatusChanged(taxiRequestId: String) = runBlocking {
        launch {
            val currentTaxiRequestResult = taxiRequestService.getCurrentTaxiRequest()

            if (currentTaxiRequestResult.isSuccessful) {
                val updatedTaxiRequest = currentTaxiRequestResult.data!!
                val cachedTaxiRequest = taxiRequestCache.getCached()
                if (updatedTaxiRequest != cachedTaxiRequest ||
                    updatedTaxiRequest.status != cachedTaxiRequest.status) {
                    taxiRequestCache.saveToCache(updatedTaxiRequest)
                    EventBus.getDefault().post(TaxiRequestStatusChangedEvent(updatedTaxiRequest))
                }
            } else if (hasNoCurrentTaxiRequest(currentTaxiRequestResult)) {
                val taxiRequestResult = taxiRequestService.getTaxiRequestById(taxiRequestId)
                if (taxiRequestResult.isSuccessful &&
                    taxiRequestResult.data?.status == Status.CANCELLED) {

                    val taxiRequest = taxiRequestResult.data
                    if (taxiRequest.status == Status.CANCELLED) {
                        taxiRequestCache.saveToCache(taxiRequest)
                        EventBus.getDefault().post(TaxiRequestStatusChangedEvent(taxiRequest))
                    }
                }
            }
        }
    }

    private fun hasNoCurrentTaxiRequest(currentTaxiRequestResult: Result<TaxiRequest, TaxiRequestRetrievalError>) =
        currentTaxiRequestResult.error == TaxiRequestRetrievalError.NOT_FOUND
}