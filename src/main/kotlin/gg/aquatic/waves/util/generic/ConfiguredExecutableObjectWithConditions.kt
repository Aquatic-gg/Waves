package gg.aquatic.waves.util.generic

import gg.aquatic.waves.util.requirement.ConfiguredRequirementWithFailActions

class ConfiguredExecutableObjectWithConditions<A, B>(
    val configuredObject: ConfiguredExecutableObject<A, B>,
    val conditions: Collection<ConfiguredRequirementWithFailActions<A,B>>,
    val failConfiguredObjects: ConfiguredExecutableObjectsWithConditions<A>?
) {

    fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
        for (condition in conditions) {
            if (!condition.tryExecute(binder, textUpdater)) {
                failConfiguredObjects?.tryExecute(binder, textUpdater)
                return
            }
        }
        configuredObject.execute(binder, textUpdater)

    }

}