package com.taksapp.taksapp.domain.events

import com.taksapp.taksapp.domain.TaxiRequest

class TaxiRequestStatusChangedEvent (val taxiRequest: TaxiRequest)