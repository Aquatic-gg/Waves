package gg.aquatic.execute

import gg.aquatic.execute.action.ActionSerializer
import gg.aquatic.execute.action.ActionSerializer.TransformedAction
import gg.aquatic.execute.requirement.ConditionSerializer
import gg.aquatic.execute.requirement.ConditionSerializer.TransformedRequirement
import gg.aquatic.execute.action.ACTIONS
import gg.aquatic.execute.requirement.CONDITIONS

class ClassTransform<T : Any, D : Any>(val clazz: Class<D>, val transform: (T) -> D) {
    fun transform(obj: T): D {
        return transform(obj)
    }

    fun createTransformedAction(id: String): TransformedAction<T, D>? {
        val action = ActionSerializer.allActions(clazz)[id]
        if (action == null) {
            if (clazz == Unit::class.java) return null
            val voidActions = ACTIONS[Unit::class.java] ?: return null
            val voidAction = voidActions[id] ?: return null
            return TransformedAction(TransformedAction(voidAction as Action<Unit>) { _ -> let {  } }, transform)
        }
        return TransformedAction(action, transform)
    }

    fun createTransformedRequirement(id: String): TransformedRequirement<T, D>? {
        val requirement = ConditionSerializer.allRequirements(clazz)[id]
        if (requirement == null) {
            if (clazz == Unit::class.java) return null
            val voidRequirements = CONDITIONS[Unit::class.java] ?: return null
            val voidRequirement = voidRequirements[id] ?: return null
            return TransformedRequirement(
                TransformedRequirement(voidRequirement as Condition<Unit>) { _ -> let { } },
                transform
            )
        }
        return TransformedRequirement(requirement, transform)
    }
}