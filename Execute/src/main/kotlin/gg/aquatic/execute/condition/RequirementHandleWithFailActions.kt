package gg.aquatic.execute.condition

import gg.aquatic.execute.ConditionalActionsHandle

class RequirementHandleWithFailActions<A,B>(
    val condition: ConditionHandle<A>,
    val failActions: ConditionalActionsHandle<A>?
) {

    suspend fun tryExecute(binder: A, textUpdater: (A, String) -> String): Boolean {
        if (!condition.execute(binder, textUpdater)) {
            failActions?.tryExecute(binder, textUpdater)
            return false
        }
        return true
    }

}