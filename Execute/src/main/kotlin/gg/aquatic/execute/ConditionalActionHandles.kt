package gg.aquatic.execute

import gg.aquatic.execute.argument.ObjectArguments
import gg.aquatic.execute.requirement.ConditionHandleWithFailActions

open class ConditionalActionHandles<A>(
    val actions: Collection<ConditionalActionHandle<A, Unit>>,
    val conditions: Collection<ConditionHandleWithFailActions<A, Unit>>,
    val failActions: ConditionalActionHandles<A>?,
): ExecutableObjectHandle<A, Unit>(
    ExecutableObjectBundle(actions.map { it.action.executableObject }),
    ObjectArguments(hashMapOf())
) {

    override fun execute(binder: A, textUpdater: (A, String) -> String) {
        tryExecute(binder, textUpdater)
    }

    fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
        for (condition in conditions) {
            if (!condition.tryExecute(binder, textUpdater)) {
                failActions?.tryExecute(binder, textUpdater)
                return
            }
        }

        for (executableObject in actions) {
            executableObject.tryExecute(binder, textUpdater)
        }
    }
}