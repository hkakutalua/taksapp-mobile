package com.taksapp.taksapp.data.webservices.client.jsonconverters

import com.squareup.moshi.*
import org.joda.time.DateTime

class MoshiDateTimeIsoAdapter : JsonAdapter<DateTime>() {
    @FromJson
    override fun fromJson(reader: JsonReader): DateTime? {
        return DateTime.parse(reader.nextString())
    }

    @ToJson
    override fun toJson(writer: JsonWriter, value: DateTime?) {
        writer.value(value?.toString())
    }
}