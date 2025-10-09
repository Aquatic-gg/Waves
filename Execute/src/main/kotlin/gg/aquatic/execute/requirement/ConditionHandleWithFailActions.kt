package gg.aquatic.execute.requirement

import gg.aquatic.execute.ConditionalActionHandles

class ConditionHandleWithFailActions<A,B>(
    val condition: ConditionHandle<A>,
    val failActions: ConditionalActionHandles<A>?
) {

    fun tryExecute(binder: A, textUpdater: (A, String) -> String): Boolean {
        if (!condition.execute(binder, textUpdater)) {
            failActions?.tryExecute(binder, textUpdater)
            return false
        }
        return true
    }

}