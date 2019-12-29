package com.taksapp.taksapp.data.webservices.client.resources.common

data class PageResponseBody<T>(val page: Int, val count: Int, val data: List<T>)