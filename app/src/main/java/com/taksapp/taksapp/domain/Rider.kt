package com.taksapp.taksapp.domain

import java.io.Serializable

class Rider(
    val id: String,
    val firstName: String,
    val lastName: String,
    val location: Location?
) : Serializable