package gg.aquatic.execute

import gg.aquatic.execute.argument.ObjectArguments
import gg.aquatic.execute.requirement.ConfiguredConditionWithFailActions

open class ConfiguredExecutableObjectsWithConditions<A>(
    val executableObjects: Collection<ConfiguredExecutableObjectWithConditions<A, Unit>>,
    val conditions: Collection<ConfiguredConditionWithFailActions<A, Unit>>,
    val failExecutableObjects: ConfiguredExecutableObjectsWithConditions<A>?,
): ConfiguredExecutableObject<A, Unit>(
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