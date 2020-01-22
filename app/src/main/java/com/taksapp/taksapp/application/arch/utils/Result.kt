package com.taksapp.taksapp.application.arch.utils

enum class Status {
    LOADING,
    FAILURE,
    SUCCESS
}

class Result<T, TError>(
    val status: Status,
    val data: T?,
    val error: TError?) {

    companion object {
        fun <T, TError> success(data: T?) = Result<T, TError>(status = Status.SUCCESS, data = data, error = null)
        fun <T, TError> error(error: TError?) = Result<T, TError>(status = Status.FAILURE, data = null, error = error)
        fun <T, TError> loading(data: T? = null) = Result<T, TError>(status = Status.LOADING, data = data, error = null)
    }

    val isSuccessful get() = status == Status.SUCCESS
    val isLoading get() = status == Status.LOADING
    val hasFailed get() = status == Status.FAILURE
}