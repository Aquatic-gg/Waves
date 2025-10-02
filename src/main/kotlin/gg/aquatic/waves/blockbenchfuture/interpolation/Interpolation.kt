package gg.aquatic.waves.blockbenchfuture.interpolation

import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.floats.FloatSortedSet
import org.joml.Math.fma
import org.joml.Vector3f
import kotlin.math.abs


object Interpolation {

    const val FRAME_HASH: Float = 10f / 1000f
    const val FRAME_HASH_REVERT: Float = 1 / FRAME_HASH
    const val FRAME_EPSILON: Float = 0.001f
    const val FLOAT_COMPARISON_EPSILON: Float = 1E-5f

    fun bezier(
        time: Float,
        startTime: Float,
        endTime: Float,
        startValue: Vector3f,
        endValue: Vector3f,
        bezierLeftTime: Vector3f?,
        bezierLeftValue: Vector3f?,
        bezierRightTime: Vector3f?,
        bezierRightValue: Vector3f?
    ): Vector3f {
        val p1 = if (bezierRightValue != null) bezierRightValue.add(startValue, Vector3f()) else startValue
        val p2 = if (bezierLeftValue != null) bezierLeftValue.add(endValue, Vector3f()) else endValue

        return Vector3f(
            cubicBezier(
                startValue.x, p1.x, p2.x, endValue.x, solveBezierTForTime(
                    time,
                    startTime,
                    if (bezierRightTime != null) bezierRightTime.x + startTime else startTime,
                    if (bezierLeftTime != null) bezierLeftTime.x + endTime else endTime,
                    endTime
                )
            ),
            cubicBezier(
                startValue.y, p1.y, p2.y, endValue.y, solveBezierTForTime(
                    time,
                    startTime,
                    if (bezierRightTime != null) bezierRightTime.y + startTime else startTime,
                    if (bezierLeftTime != null) bezierLeftTime.y + endTime else endTime,
                    endTime
                )
            ),
            cubicBezier(
                startValue.z, p1.z, p2.z, endValue.z, solveBezierTForTime(
                    time,
                    startTime,
                    if (bezierRightTime != null) bezierRightTime.z + startTime else startTime,
                    if (bezierLeftTime != null) bezierLeftTime.z + endTime else endTime,
                    endTime
                )
            )
        )
    }

    fun catmullrom(p0: Vector3f, p1: Vector3f, p2: Vector3f, p3: Vector3f, t: Float): Vector3f {
        val t2 = t * t
        val t3 = t2 * t
        return Vector3f(
            fma(
                t3,
                fma(-1f, p0.x, fma(3f, p1.x, fma(-3f, p2.x, p3.x))),
                fma(t2, fma(2f, p0.x, fma(-5f, p1.x, fma(4f, p2.x, -p3.x))), fma(t, -p0.x + p2.x, 2f * p1.x))
            ),
            fma(
                t3,
                fma(-1f, p0.y, fma(3f, p1.y, fma(-3f, p2.y, p3.y))),
                fma(t2, fma(2f, p0.y, fma(-5f, p1.y, fma(4f, p2.y, -p3.y))), fma(t, -p0.y + p2.y, 2f * p1.y))
            ),
            fma(
                t3,
                fma(-1f, p0.z, fma(3f, p1.z, fma(-3f, p2.z, p3.z))),
                fma(t2, fma(2f, p0.z, fma(-5f, p1.z, fma(4f, p2.z, -p3.z))), fma(t, -p0.z + p2.z, 2f * p1.z))
            )
        ).mul(0.5f)
    }

    fun roundTime(time: Float): Float {
        return fma(time, FRAME_HASH_REVERT, FRAME_EPSILON).toInt() * FRAME_HASH
    }

    fun insertLerpFrame(frames: FloatSortedSet) {
        insertLerpFrame(frames, 3f / 20f)
    }

    fun insertLerpFrame(frames: FloatSortedSet, frame: Float) {
        if (frame <= 0f) return
        var first = 0f
        var second = 0f
        val iterator = FloatArrayList(frames).iterator()
        while (iterator.hasNext()) {
            first = second
            second = iterator.nextFloat()
            val max = ((second - first) / frame).toInt()
            for (i in 0..<max) {
                val add = fma(frame, i + 1f, first)
                if (second - add < frame + FRAME_EPSILON) continue
                frames.add(add)
            }
        }
    }

    fun alpha(p0: Float, p1: Float, alpha: Float): Float {
        val div = p1 - p0
        return if (div == 0f) 0f else (alpha - p0) / div
    }

    fun lerp(p0: Vector3f, p1: Vector3f, alpha: Float): Vector3f {
        return Vector3f(
            lerp(p0.x, p1.x, alpha),
            lerp(p0.y, p1.y, alpha),
            lerp(p0.z, p1.z, alpha)
        )
    }

    fun lerp(p0: Float, p1: Float, alpha: Float): Float {
        return fma(p1 - p0, alpha, p0)
    }

    private fun cubicBezier(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val u = 1.0f - t
        val uu = u * u
        val tt = t * t
        val uuu = uu * u
        val utt = u * tt
        val uut = uu * t
        val ttt = tt * t
        return fma(uuu, p0, fma(3.0f * uut, p1, fma(3.0f * utt, p2, ttt * p3)))
    }

    private fun derivativeBezier(p0: Float, p1: Float, p2: Float, p3: Float, t: Float): Float {
        val u = 1.0f - t
        val uu = u * u
        val ut = u * t
        val tt = t * t
        return fma(3.0f * uu, p1 - p0, fma(6.0f * ut, p2 - p1, 3.0f * tt * (p3 - p2)))
    }

    private fun solveBezierTForTime(time: Float, t0: Float, h1: Float, h2: Float, t1: Float): Float {
        var t = 0.5f
        val maxIterations = 20
        for (i in 0..<maxIterations) {
            val bezTime = cubicBezier(t0, h1, h2, t1, t)
            val derivative = derivativeBezier(t0, h1, h2, t1, t)
            val error = bezTime - time
            if (abs(error) < FLOAT_COMPARISON_EPSILON) {
                return t
            }
            if (derivative != 0f) {
                t -= error / derivative
            }
            t = Math.clamp(t, 0f, 1f)
        }

        return t
    }

    fun fma(a: Vector3f, b: Vector3f, c: Vector3f): Vector3f {
        a.x = fma(a.x, b.x, c.x)
        a.y = fma(a.y, b.y, c.y)
        a.z = fma(a.z, b.z, c.z)
        return a
    }

    fun fma(a: Vector3f, b: Float, c: Vector3f): Vector3f {
        a.x = fma(a.x, b, c.x)
        a.y = fma(a.y, b, c.y)
        a.z = fma(a.z, b, c.z)
        return a
    }
}
