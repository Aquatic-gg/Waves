package gg.aquatic.execute

import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.execute.condition.ConditionHandleWithFailActions

open class ConditionalActionsHandle<A>(
    val executableObjects: Collection<ConditionalActionHandle<A, Unit>>,
    val conditions: Collection<ConditionHandleWithFailActions<A>>,
    val failExecutableObjects: ConditionalActionsHandle<A>?,
): ExecutableObjectHandle<A, Unit>(
    ExecutableObjectBundle(executableObjects.map { it.configuredObject.executableObject }),
    ObjectArguments(hashMapOf())
) {

    override suspend fun execute(binder: A, textUpdater: (A, String) -> String) {
        tryExecute(binder, textUpdater)
    }

    suspend fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
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