package com.clerami.universe.utils

import com.clerami.universe.data.remote.response.Timestamp
import com.google.gson.*
import java.lang.reflect.Type


class TimestampDeserializer : JsonDeserializer<Timestamp> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): Timestamp {
        if (json == null || json.isJsonNull) {
            return Timestamp(0, 0)  // Return default Timestamp if it's null
        }

        return if (json.isJsonObject) {
            val jsonObject = json.asJsonObject
            val seconds = jsonObject.get("_seconds").asLong
            val nanoseconds = jsonObject.get("_nanoseconds").asLong
            Timestamp(seconds, nanoseconds)
        } else {
            // Fallback if the structure doesn't match
            Timestamp(0, 0)
        }
    }
}
