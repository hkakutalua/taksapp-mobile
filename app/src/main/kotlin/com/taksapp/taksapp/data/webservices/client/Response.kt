package com.taksapp.taksapp.data.webservices.client

class Response<T, TError>(
    val successful: Boolean,
    val data: T?,
    val error: TError?) {

    companion object {
        fun <T, TErrorCode> success(data: T?) : Response<T, TErrorCode> {
            return Response(
                successful = true,
                data = data,
                error = null
            )
        }

        fun <T, TError> failure(error: TError) : Response<T, TError> {
            return Response(
                successful = false,
                data = null,
                error = error
            )
        }
    }
}