package com.taksapp.taksapp.data.webservices.client.jsonconverters

import com.squareup.moshi.*
import java.math.BigDecimal

class BigDecimalJsonAdapter : JsonAdapter<BigDecimal>() {
    @FromJson
    override fun fromJson(reader: JsonReader): BigDecimal? {
        return BigDecimal(reader.nextDouble())
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: BigDecimal?) {
        writer.value(value?.toDouble())
    }
}