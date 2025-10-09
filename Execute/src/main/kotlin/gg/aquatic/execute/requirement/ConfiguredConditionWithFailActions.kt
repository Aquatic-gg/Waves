package gg.aquatic.execute.requirement

import gg.aquatic.execute.ConfiguredExecutableObjectsWithConditions

class ConfiguredConditionWithFailActions<A,B>(
    val condition: ConfiguredCondition<A>,
    val failActions: ConfiguredExecutableObjectsWithConditions<A>?
) {

    fun tryExecute(binder: A, textUpdater: (A, String) -> String): Boolean {
        if (!condition.execute(binder, textUpdater)) {
            failActions?.tryExecute(binder, textUpdater)
            return false
        }
        return true
    }

}