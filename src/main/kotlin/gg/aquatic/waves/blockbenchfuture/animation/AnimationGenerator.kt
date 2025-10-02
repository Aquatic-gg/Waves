package gg.aquatic.waves.blockbenchfuture.animation

import gg.aquatic.waves.blockbenchfuture.blueprint.BlueprintAnimator
import gg.aquatic.waves.blockbenchfuture.blueprint.BlueprintChildren
import gg.aquatic.waves.blockbenchfuture.interpolation.Interpolation
import it.unimi.dsi.fastutil.floats.FloatAVLTreeSet
import kotlin.math.abs

object AnimationGenerator {

    fun createMovements(
        length: Float,
        children: Map<String, BlueprintChildren>,
        pointMap: Map<String, BlueprintAnimator.AnimatorData>,
    ) {
        val floatSet =
            FloatAVLTreeSet(pointMap.values.flatMap { animatorData -> animatorData.allPoints().map { it.time } }
                .toFloatArray()) { a, b ->
                if (abs(a - b) < Interpolation.FLOAT_COMPARISON_EPSILON) 0 else a.compareTo(
                    b
                )
            }
        floatSet.add(0f)
        floatSet.add(length)
        Interpolation.insertLerpFrame(floatSet)

    }

    private class Handle(
        val pointMap: Map<String, BlueprintAnimator.AnimatorData>,
        val children: Map<String, BlueprintChildren>
    ) {



    }

}