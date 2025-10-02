package gg.aquatic.waves.blockbenchfuture.raw

import com.google.gson.annotations.SerializedName
import kotlin.math.abs

data class ModelElement(
    val name: String,
    val type: Type?,
    val uuid: String,
    val from: Float3?,
    val to: Float3?,
    val inflate: Float,
    val rotation: Float3?,
    val origin: Float3?,
    val faces: ModelFace?,
    val visibility: Boolean?
) {

    enum class Type {
        @SerializedName("cube")
        CUBE,
        @SerializedName("mesh")
        MESH
    }

    fun from(): Float3 {
        return from ?: Float3(0f, 0f, 0f)
    }
    fun to(): Float3 {
        return to ?: Float3(0f, 0f, 0f)
    }

    fun max(origin: Float3): Float {
        val from = from().minus(origin)
        val to = to().minus(origin)
        var max = 0f
        max = max.coerceAtLeast(abs(from.x))
        max = max.coerceAtLeast(abs(from.y))
        max = max.coerceAtLeast(abs(from.z))
        max = max.coerceAtLeast(abs(to.x))
        max = max.coerceAtLeast(abs(to.y))
        max = max.coerceAtLeast(abs(to.z))
        return max
    }

    fun rotation(): Float3 {
        return rotation ?: Float3(0f, 0f, 0f)
    }

    fun type(): Type {
        return type ?: Type.CUBE
    }

    fun hasTexture(): Boolean {
        return faces?.hasTexture() ?: false
    }

    fun visible(): Boolean {
        return visibility ?: true
    }

}