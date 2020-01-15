package com.taksapp.taksapp.data.webservices.client.exceptions

import com.taksapp.taksapp.data.webservices.client.resources.common.ValidationErrorBody

class ValidationErrorException (body: ValidationErrorBody) : Exception()