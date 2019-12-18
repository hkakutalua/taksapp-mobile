package com.taksapp.taksapp.data.webservices.client.jsonconverters

import java.io.InputStream
import java.lang.reflect.Type
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

    /**
     * Converts the JSON contained in the [stream] to a Kotlin generic closed object
     * @param stream that contains the JSON
     * @param kClass the class that represents the generic Kotlin type to convert the JSON to
     * @param typeParams the type parameters of [kClass]
     * @param T the Kotlin type to convert the JSON to
     * @return an instance of [T] from the JSON
     */
    fun <T: Any> fromJson(
        stream: InputStream,
        kClass: KClass<T>,
        rootType: Type,
        vararg typeParams: Type): T
}