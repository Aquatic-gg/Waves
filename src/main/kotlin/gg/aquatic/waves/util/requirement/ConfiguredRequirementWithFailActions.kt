package gg.aquatic.waves.util.requirement

import gg.aquatic.waves.util.generic.ConfiguredExecutableObjectsWithConditions

class ConfiguredRequirementWithFailActions<A,B>(
    val condition: ConfiguredRequirement<A>,
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