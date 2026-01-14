package gg.aquatic.execute

import gg.aquatic.execute.action.ActionSerializer.TransformedAction
import gg.aquatic.execute.action.getActions
import gg.aquatic.execute.action.getHierarchical
import gg.aquatic.execute.requirement.ConditionSerializer.TransformedCondition
import gg.aquatic.execute.requirement.getHierarchical

class ClassTransform<T : Any, D : Any>(val clazz: Class<D>, val transform: (T) -> D) {
    fun transform(obj: T): D {
        return transform(obj)
    }

    fun createTransformedAction(id: String): TransformedAction<T, D>? {

        Action.REGISTRY.getHierarchical(id, clazz) ?: return null
        val action = Action.REGISTRY.getHierarchical(id, clazz)
        if (action == null) {
            if (clazz == Unit::class.java) return null

            val voidActions = Action.Companion.REGISTRY.getActions<Unit>()
            val voidAction = voidActions[id] ?: return null
            return TransformedAction(TransformedAction(voidAction) { d -> let { } }, transform)
        }
        return TransformedAction(action, transform)
    }

    fun createTransformedCondition(id: String): TransformedCondition<T, D>? {
        val requirement = Condition.REGISTRY.getHierarchical(id, clazz) ?: return null
        return TransformedCondition(requirement, transform)
    }
}