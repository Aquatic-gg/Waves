package gg.aquatic.waves.blockbench.raw

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializer


data class Float4(
    val dx: Float,
    val dz: Float,
    val tx: Float,
    val tz: Float,
) {

    companion object {
        val PARSER = JsonDeserializer<Float4> { json, _, _ ->
            val array = json.getAsJsonArray()
            return@JsonDeserializer Float4(
                array.get(0).asFloat,
                array.get(1).asFloat,
                array.get(2).asFloat,
                array.get(3).asFloat
            )
        }
    }

    fun div(resolution: ModelResolution): Float4 {
        return div(
            resolution.width.toFloat() / 16f,
            resolution.height.toFloat() / 16f
        )
    }

    fun div(width: Float, height: Float): Float4 {
        return Float4(dx / width, dz / width, tx / height, tz / height)
    }

    fun toJson(): JsonArray {
        val array = JsonArray(4)
        array.add(dx)
        array.add(dz)
        array.add(tx)
        array.add(tz)
        return array
    }
}