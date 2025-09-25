package gg.aquatic.waves.blockbench.raw

import com.google.gson.annotations.SerializedName
import gg.aquatic.waves.blockbench.interpolation.Timed
import gg.aquatic.waves.blockbench.interpolation.VectorInterpolation

data class ModelKeyframe(
    val channel: KeyframeChannel?,
    @SerializedName("data_points") val dataPoints: List<Datapoint>,
    @SerializedName("bezier_left_time") val bezierLeftTime: Float3?,
    @SerializedName("bezier_left_value") val bezierLeftValue: Float3?,
    @SerializedName("bezier_right_time") val bezierRightTime: Float3?,
    @SerializedName("bezier_right_value") val bezierRightValue: Float3?,
    val interpolation: String?,
    override val time: Float
): Timed {

    fun point(): Datapoint {
        return dataPoints.first()
    }

    fun interpolator(): VectorInterpolation {
        return when (interpolation) {
            "linear" -> VectorInterpolation.Linear()
            "step" -> VectorInterpolation.Step()
            "catmullrom" -> VectorInterpolation.Catmullrom()
            "bezier" -> VectorInterpolation.Bezier(
                bezierLeftTime?.toVector(),
                bezierLeftValue?.toVector(),
                bezierRightTime?.toVector(),
                bezierRightValue?.toVector()
            )
            else -> VectorInterpolation.Linear()
        }
    }

    fun channel(): KeyframeChannel {
        return channel ?: KeyframeChannel.UNKNOWN
    }

}