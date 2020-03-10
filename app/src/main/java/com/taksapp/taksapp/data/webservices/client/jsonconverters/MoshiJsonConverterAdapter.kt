package com.taksapp.taksapp.data.webservices.client.jsonconverters

import com.squareup.moshi.*
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okio.buffer
import okio.source
import java.io.InputStream
import java.lang.reflect.Type
import kotlin.reflect.KClass

class MoshiJsonConverterAdapter : JsonConverter {
    @ToJson
    override fun toJson(body: Any): String {
        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()
            .adapter(body.javaClass)
            .toJson(body)
    }

    @ToJson
    override fun <T : Any> toJson(
        body: T,
        kClass: KClass<T>,
        rootType: Type,
        vararg typeParams: Type
    ): String {
        val parameterizedType = Types.newParameterizedType(rootType, *typeParams)

        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(BigDecimalJsonAdapter())
            .build()
            .adapter<T>(parameterizedType)
            .toJson(body)
    }

    @FromJson
    override fun <T : Any> fromJson(stream: InputStream, kClass: KClass<T>): T {
        val bufferedSource = stream.source().buffer()

        return Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(MoshiDateTimeIsoAdapter())
            .add(BigDecimalJsonAdapter())
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
            .add(BigDecimalJsonAdapter())
            .build()
            .adapter<T>(parameterizedType)
            .fromJson(JsonReader.of(bufferedSource))!!
    }
}