package gg.aquatic.execute

import gg.aquatic.execute.requirement.ConditionHandleWithFailActions

class ConditionalActionHandle<A, B>(
    val action: ExecutableObjectHandle<A, B>,
    val conditions: Collection<ConditionHandleWithFailActions<A,B>>,
    val failActions: ConditionalActionHandles<A>?
) {

    fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
        for (condition in conditions) {
            if (!condition.tryExecute(binder, textUpdater)) {
                failActions?.tryExecute(binder, textUpdater)
                return
            }
        }
        action.execute(binder, textUpdater)

    }

}