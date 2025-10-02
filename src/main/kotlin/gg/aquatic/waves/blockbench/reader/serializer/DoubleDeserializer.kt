package gg.aquatic.waves.blockbench.reader.serializer

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import java.lang.reflect.Type

class DoubleDeserializer: JsonDeserializer<Double> {
    @Throws(JsonParseException::class)
    override fun deserialize(
        jsonElement: JsonElement,
        type: Type?,
        jsonDeserializationContext: JsonDeserializationContext?
    ): Double? {
        val str = jsonElement.asString
        if (str == null || str.isEmpty() || str.isBlank()) {
            return null
        }
        return str.toDouble()
    }
}