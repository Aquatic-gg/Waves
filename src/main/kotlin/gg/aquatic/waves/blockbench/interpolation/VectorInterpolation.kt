package gg.aquatic.waves.blockbench.interpolation

import org.joml.Vector3f


interface VectorInterpolation {

    fun interpolate(points: Collection<VectorPoint>, p2Index: Int, time: Float): Vector3f

    fun isContinuous(): Boolean {
        return true
    }

    class Linear : VectorInterpolation {
        override fun interpolate(points: Collection<VectorPoint>, p2Index: Int, time: Float): Vector3f {
            TODO("Not yet implemented")
        }
    }

    class Step : VectorInterpolation {
        override fun interpolate(points: Collection<VectorPoint>, p2Index: Int, time: Float): Vector3f {
            return (if (p2Index > 0) points.elementAt(p2Index - 1) else VectorPoint.EMPTY).vector(time)
        }

        override fun isContinuous(): Boolean {
            return false
        }
    }

    class Catmullrom : VectorInterpolation {

        private fun indexOf(list: Collection<VectorPoint>, index: Int, relative: Int): VectorPoint {
            var i = index + relative
            while (i < 0) i += list.size
            return list.elementAt(i % list.size)
        }

        override fun interpolate(
            points: Collection<VectorPoint>,
            p2Index: Int,
            time: Float,
        ): Vector3f {
            val p0 = indexOf(points, p2Index, -2)
            val p1 = indexOf(points, p2Index, -1)
            val p2 = points.elementAt(p2Index)
            val p3 = indexOf(points, p2Index, 1)

            val t1 = p1.time
            val t2 = p2.time
            val a = Interpolation.alpha(t1, t2, time)

            return Interpolation.catmullrom(
                p0.vector(),
                p1.vector(Interpolation.lerp(t1, t2, a)),
                p2.vector(),
                p3.vector(),
                a
            )
        }
    }

    class Bezier(
        val bezierLeftTime: Vector3f? = null,
        val bezierLeftValue: Vector3f? = null,
        val bezierRightTime: Vector3f? = null,
        val bezierRightValue: Vector3f? = null
    ) : VectorInterpolation {

        override fun interpolate(
            points: Collection<VectorPoint>,
            p2Index: Int,
            time: Float,
        ): Vector3f {
            val p1 = if (p2Index > 0) points.elementAt(p2Index - 1) else VectorPoint.Companion.EMPTY
            val p2 = points.elementAt(p2Index)

            val t1 = p1.time
            val t2 = p2.time
            val a = Interpolation.alpha(t1, t2, time)

            return Interpolation.bezier(
                time,
                t1,
                t2,
                p1.vector(Interpolation.lerp(t1, t2, a)),
                p2.vector(),
                bezierLeftTime,
                bezierLeftValue,
                bezierRightTime,
                bezierRightValue
            )
        }
    }
}