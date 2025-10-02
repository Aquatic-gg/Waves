package gg.aquatic.waves.blockbench.reader.serializer

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import gg.aquatic.waves.blockbench.reader.data.BBBone
import gg.aquatic.waves.blockbench.reader.data.BBChildren
import gg.aquatic.waves.blockbench.reader.data.BBElementChildren
import java.lang.reflect.Type
import java.util.*

class ChildrenDeserializer(
    val gson: GsonBuilder
): JsonDeserializer<BBChildren> {
    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext?): BBChildren {
        if (jsonElement.isJsonObject) {
            return gson.create().fromJson(jsonElement, BBBone::class.java)
        }
        return BBElementChildren(UUID.fromString(jsonElement.asString))
    }
}