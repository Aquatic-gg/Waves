package gg.aquatic.waves.blockbench.raw

import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import org.joml.Vector3f

data class Datapoint(
    val x: JsonPrimitive?,
    val y: JsonPrimitive?,
    val z: JsonPrimitive?,
    val script: String?,
) {

    companion object {
        fun build(primitive: JsonPrimitive?, placeholder: ModelPlaceholder): (Float) -> Float {
            if (primitive == null) return { 0f }
            if (primitive.isNumber) return { primitive.asFloat }
            val str = primitive.asString.trim()
            if (str.isEmpty()) return { 0f }

            return str.toFloatOrNull()?.let {
                {it}
            } ?: { 0f }
        }

        val PARSER = JsonDeserializer<Datapoint> { json, _, _ ->
            val obj = json.asJsonObject
            val script = obj.getAsJsonPrimitive("script")
            Datapoint(
                obj.getAsJsonPrimitive("x"),
                obj.getAsJsonPrimitive("y"),
                obj.getAsJsonPrimitive("z"),
                script?.asString
            )
        }
    }

    fun toFunction(placeholder: ModelPlaceholder): (Float) -> Vector3f {
        val xb = build(x, placeholder)
        val yb = build(y, placeholder)
        val zb = build(z, placeholder)
        return { t ->
            Vector3f(xb(t), yb(t), zb(t))
        }
    }

}