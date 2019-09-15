package com.taksapp.taksapp.data.webservices.client

public class Response<T, TErrorCode>(
    val successful: Boolean,
    val data: T?,
    val errorCode: TErrorCode?) {

    companion object {
        fun <T, TErrorCode> success(data: T?) : Response<T, TErrorCode> {
            return Response(
                successful = true,
                data = data,
                errorCode = null
            )
        }

        fun <T, TErrorCode> failure(errorCode: TErrorCode) : Response<T, TErrorCode> {
            return Response(
                successful = false,
                data = null,
                errorCode = errorCode
            )
        }
    }
}