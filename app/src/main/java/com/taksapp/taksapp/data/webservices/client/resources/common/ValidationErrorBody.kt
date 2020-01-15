package com.taksapp.taksapp.data.webservices.client.resources.common

class ValidationErrorBody (
    val type: String,
    val title: String,
    val status: Int,
    val traceId: String,
    val errors: Map<String, List<String>>
)