package com.taksapp.taksapp.data.webservices.client.jsonconverters

import com.squareup.moshi.JsonReader
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.buffer
import okio.source
import java.io.InputStream
import java.lang.reflect.Type
import kotlin.reflect.KClass

class MoshiJsonConverterAdapter : JsonConverter {
    override fun toJson(body: Any): String {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(body.javaClass)
            .toJson(body)
    }

    override fun <T : Any> fromJson(stream: InputStream, kClass: KClass<T>): T {
        val bufferedSource = stream.source().buffer()

        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(kClass.java)
            .fromJson(JsonReader.of(bufferedSource))!!
    }

    override fun <T : Any> fromJson(
        stream: InputStream,
        kClass: KClass<T>,
        rootType: Type,
        vararg typeParams: Type): T {

        val bufferedSource = stream.source().buffer()

        val parameterizedType = Types.newParameterizedType(rootType, *typeParams)

        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter<T>(parameterizedType)
            .fromJson(JsonReader.of(bufferedSource))!!
    }
}