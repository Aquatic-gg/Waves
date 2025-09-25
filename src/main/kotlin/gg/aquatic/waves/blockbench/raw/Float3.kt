package gg.aquatic.waves.blockbench.raw

import com.google.gson.JsonArray
import com.google.gson.JsonDeserializer
import gg.aquatic.waves.util.math.toXYZEuler
import org.joml.Quaternionf
import org.joml.Vector3f

data class Float3(
    val x: Float,
    val y: Float = x,
    val z: Float = x,
) {

    companion object {
        val ZERO = Float3(0f)
        val PARSER = JsonDeserializer<Float3> { json, _, _ ->
            val array = json.getAsJsonArray()
            Float3(
                array.get(0).asFloat,
                array.get(1).asFloat,
                array.get(2).asFloat
            )
        }
    }

    operator fun plus(other: Float3): Float3 {
        return Float3(x + other.x, y + other.y, z + other.z)
    }

    operator fun minus(other: Float3): Float3 {
        return Float3(x - other.x, y - other.y, z - other.z)
    }

    operator fun times(other: Float3): Float3 {
        return Float3(x * other.x, y * other.y, z * other.z)
    }
    operator fun div(other: Float3): Float3 {
        return Float3(x / other.x, y / other.y, z / other.z)
    }

    fun times(value: Float): Float3 {
        return Float3(x * value, y * value, z * value)
    }

    fun div(value: Float): Float3 {
        return Float3(x / value, y / value, z / value)
    }

    fun toBlockScale(): Float3 {
        return div(16f)
    }

    fun invertXZ(): Float3 {
        return Float3(-x, y, -z)
    }

    fun toXYZEuler(): Float3 {
        val vector = toVector().toXYZEuler()
        return Float3(vector.x, vector.y, vector.z)
    }

    fun toVector(): Vector3f {
        return Vector3f(x, y, z)
    }

    fun rotate(quaternionf: Quaternionf): Float3 {
        val vec = toVector().rotate(quaternionf)
        return Float3(vec.x, vec.y, vec.z)
    }

    fun toJson(): JsonArray {
        val array = JsonArray(3)
        array.add(x)
        array.add(y)
        array.add(z)
        return array
    }
}