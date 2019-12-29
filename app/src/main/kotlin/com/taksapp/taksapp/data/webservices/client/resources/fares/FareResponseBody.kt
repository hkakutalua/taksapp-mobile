package com.taksapp.taksapp.data.webservices.client.resources.fares

data class FareResponseBody(
    val company: CompanyResponseBody,
    val currency: String,
    val amount: Double
)

data class CompanyResponseBody(val id: String, val name: String, val image: String)