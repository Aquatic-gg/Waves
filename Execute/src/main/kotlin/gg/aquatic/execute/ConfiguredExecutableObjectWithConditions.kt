package gg.aquatic.execute

import gg.aquatic.execute.requirement.ConfiguredConditionWithFailActions

class ConfiguredExecutableObjectWithConditions<A, B>(
    val configuredObject: ConfiguredExecutableObject<A, B>,
    val conditions: Collection<ConfiguredConditionWithFailActions<A,B>>,
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