package gg.aquatic.waves.blockbench.reader.serializer

import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import gg.aquatic.waves.blockbench.reader.data.BBChildren
import java.lang.reflect.Type
import java.util.*

class ChildrenDeserializer(
    val gson: GsonBuilder
): JsonDeserializer<BBChildren> {
    override fun deserialize(jsonElement: JsonElement, type: Type, jsonDeserializationContext: JsonDeserializationContext?): BBChildren {
        if (jsonElement.isJsonObject) {
            return gson.create().fromJson(jsonElement, BBChildren.BBBoneChildren::class.java)
        }
        return BBChildren.BBElementChildren(UUID.fromString(jsonElement.asString))
    }
}