package com.taksapp.taksapp.data.webservices.client.jsonconverters

import java.io.InputStream
import kotlin.reflect.KClass

interface JsonConverter {
    /**
     * Converts an Kotlin [body] object to a JSON string
     * @param body the JSON body
     * @return the JSON string
     */
    fun toJson(body: Any): String

    /**
     * Converts the JSON contained in the [stream] to Kotlin object
     * @param stream that contains the JSON
     * @param kClass the class that represents the Kotlin type to convert the JSON to
     * @param T the Kotlin type to convert the JSON to
     * @return an instance of [T] from the JSON
     */
    fun <T: Any> fromJson(stream: InputStream, kClass: KClass<T>): T
}