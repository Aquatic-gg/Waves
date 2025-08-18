package gg.aquatic.waves.util.generic

import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.serializer.ActionSerializer
import gg.aquatic.waves.registry.serializer.ActionSerializer.TransformedAction
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.registry.serializer.RequirementSerializer.TransformedRequirement

class ClassTransform<T : Any, D : Any>(val clazz: Class<D>, val transform: (T) -> D) {
    fun transform(obj: T): D {
        return transform(obj)
    }

    fun createTransformedAction(id: String): TransformedAction<T, D>? {
        val action = ActionSerializer.allActions(clazz)[id]
        if (action == null) {
            if (clazz == Unit::class.java) return null
            val voidActions = WavesRegistry.ACTION[Unit::class.java] ?: return null
            val voidAction = voidActions[id] ?: return null
            return TransformedAction(TransformedAction(voidAction as Action<Unit>) { d -> let {  } }, transform)
        }
        return TransformedAction(action, transform)
    }

    fun createTransformedRequirement(id: String): TransformedRequirement<T, D>? {
        val requirement = RequirementSerializer.allRequirements(clazz)[id]
        if (requirement == null) {
            if (clazz == Unit::class.java) return null
            val voidRequirements = WavesRegistry.REQUIREMENT[Unit::class.java] ?: return null
            val voidRequirement = voidRequirements[id] ?: return null
            return TransformedRequirement(
                TransformedRequirement(voidRequirement as Condition<Unit>) { _ -> let { } },
                transform
            )
        }
        return TransformedRequirement(requirement, transform)
    }
}