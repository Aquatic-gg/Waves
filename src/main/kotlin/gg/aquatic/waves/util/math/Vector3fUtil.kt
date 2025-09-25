package gg.aquatic.waves.util.math

import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2

fun Vector3f.toQuaternionf(): Quaternionf {
    return Quaternionf().rotateZYX(this.z.toRadiansf(), this.y.toRadiansf(), this.x.toRadiansf())
}

fun Vector3f.toXYZEuler(): Vector3f {
    return this.toQuaternionf().toXYZEuler()
}

fun Quaternionf.toXYZEuler(): Vector3f {
    return this.toMatrix().toXYZEuler()
}

fun Quaternionf.toMatrix(): Matrix4f {
    return this.get(Matrix4f())
}

fun Matrix4f.toXYZEuler(): Vector3f {
    val ret = Vector3f()
    if (abs(this.m20()) < 1f) {
        ret.x = atan2(-this.m21().toDouble(), this.m22().toDouble()).toFloat()
        ret.z = atan2(-this.m10().toDouble(), this.m00().toDouble()).toFloat()
    } else {
        ret.x = atan2(this.m12().toDouble(), this.m11().toDouble()).toFloat()
        ret.z = 0f
    }
    ret.y = asin(Math.clamp(this.m20(), -1f, 1f).toDouble()).toFloat()
    return ret.mul(57.29578f)
}

fun Vector3f.identifier(): Vector3f {
    if (!this.checkValidDegree()) return Vector3f()
    return this
}

fun Vector3f.checkValidDegree(): Boolean {
    var i = 0
    if (this.x != 0f) i++
    if (this.y != 0f) i++
    if (this.z != 0f) i++
    return i < 2 && this.x.checkValidDegree() && this.y.checkValidDegree() && this.z.checkValidDegree()
}

private val ROTATION_DEGREE = 22.5f
private val VALID_DEGREES = arrayOf(0f, ROTATION_DEGREE, ROTATION_DEGREE * 2f, -ROTATION_DEGREE, -ROTATION_DEGREE * 2f)

fun Float.checkValidDegree(): Boolean {
    return VALID_DEGREES.contains(this)
}