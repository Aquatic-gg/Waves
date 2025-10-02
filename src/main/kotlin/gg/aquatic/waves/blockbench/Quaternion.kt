package gg.aquatic.waves.blockbench

import org.bukkit.util.EulerAngle
import kotlin.math.*

class Quaternion(eulerAngle: EulerAngle) {

    private var x = 0f

    /**
     * The second component of the vector part.
     */
    private var y = 0f

    /**
     * The third component of the vector part.
     */
    private var z = 0f

    /**
     * The real/scalar part of the quaternion.
     */
    private var w = 0f

    fun Quaternion() {
        w = 1.0f
    }

    init {
        w = 1.0f
        rotationXYZ(eulerAngle.x.toFloat(), eulerAngle.y.toFloat(), eulerAngle.z.toFloat())
    }


    fun rotationXYZ(angleX: Float, angleY: Float, angleZ: Float): Quaternion {
        val cX = cos(angleX * 0.5)
        val cY = cos(angleY * -0.5)
        val cZ = cos(angleZ * 0.5)
        val sX = sin(angleX * 0.5)
        val sY = sin(angleY * -0.5)
        val sZ = sin(angleZ * 0.5)

        x = (sX * cY * cZ + cX * sY * sZ).toFloat()
        y = (cX * sY * cZ - sX * cY * sZ).toFloat()
        z = (cX * cY * sZ + sX * sY * cZ).toFloat()
        w = (cX * cY * cZ - sX * sY * sZ).toFloat()
        return this
    }

    fun rotationXYZ(eulerAngle: EulerAngle): Quaternion {
        return rotationXYZ(eulerAngle.x.toFloat(), eulerAngle.y.toFloat(), eulerAngle.z.toFloat())
    }

    fun mul(q: Quaternion): Quaternion {
        return set(
            Math.fma(w, q.x(), Math.fma(x, q.w(), Math.fma(y, q.z(), -z * q.y()))),
            Math.fma(w, q.y(), Math.fma(-x, q.z(), Math.fma(y, q.w(), z * q.x()))),
            Math.fma(w, q.z(), Math.fma(x, q.y(), Math.fma(-y, q.x(), z * q.w()))),
            Math.fma(w, q.w(), Math.fma(-x, q.x(), Math.fma(-y, q.y(), -z * q.z())))
        )
    }

    fun set(x: Float, y: Float, z: Float, w: Float): Quaternion {
        this.x = x
        this.y = y
        this.z = z
        this.w = w
        return this
    }

    fun getEulerAnglesXYZ(): EulerAngle {
        val x2 = (x + x).toDouble()
        val y2 = (y + y).toDouble()
        val z2 = (z + z).toDouble()
        val xx = x * x2
        val xy = x * y2
        val xz = x * z2
        val yy = y * y2
        val yz = y * z2
        val zz = z * z2
        val wx = w * x2
        val wy = w * y2
        val wz = w * z2

        val ex: Double
        val ey: Double
        val ez: Double
        val m11 = 1 - (yy + zz)
        val m12 = xy - wz
        val m13 = xz + wy
        val m22 = 1 - (xx + zz)
        val m23 = yz - wx
        val m32 = yz + wx
        val m33 = 1 - (xx + yy)

        ey = asin(Math.clamp(m13, -1.0, 1.0))
        if (abs(m13) < 0.9999999) {
            ex = atan2(-m23, m33)
            ez = atan2(-m12, m11)
        } else {
            ex = atan2(m32, m22)
            ez = 0.0
        }

        return EulerAngle(ex, -ey, ez)
    }

    fun w(): Float {
        return w
    }

    fun x(): Float {
        return x
    }

    fun y(): Float {
        return y
    }

    fun z(): Float {
        return z
    }

}