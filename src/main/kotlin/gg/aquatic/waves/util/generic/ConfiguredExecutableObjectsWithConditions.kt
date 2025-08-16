package gg.aquatic.waves.util.generic

import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.requirement.ConfiguredRequirementWithFailActions

open class ConfiguredExecutableObjectsWithConditions<A>(
    val executableObjects: Collection<ConfiguredExecutableObjectWithConditions<A, Unit>>,
    val conditions: Collection<ConfiguredRequirementWithFailActions<A, Unit>>,
    val failExecutableObjects: ConfiguredExecutableObjectsWithConditions<A>?,
): ConfiguredExecutableObject<A,Unit>(
    ExecutableObjectBundle(executableObjects.map { it.configuredObject.executableObject }),
    ObjectArguments(hashMapOf())
) {

    override fun execute(binder: A, textUpdater: (A, String) -> String) {
        tryExecute(binder, textUpdater)
    }

    fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
        for (condition in conditions) {
            if (!condition.tryExecute(binder, textUpdater)) {
                failExecutableObjects?.tryExecute(binder, textUpdater)
                return
            }
        }

        for (executableObject in executableObjects) {
            executableObject.tryExecute(binder, textUpdater)
        }
    }
}