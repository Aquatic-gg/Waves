package gg.aquatic.execute

import gg.aquatic.execute.condition.RequirementHandleWithFailActions

class ConditionalActionHandle<A, B>(
    val configuredObject: ExecutableObjectHandle<A, B>,
    val conditions: Collection<RequirementHandleWithFailActions<A,B>>,
    val failConfiguredObjects: ConditionalActionsHandle<A>?
) {

    suspend fun tryExecute(binder: A, textUpdater: (A, String) -> String) {
        for (condition in conditions) {
            if (!condition.tryExecute(binder, textUpdater)) {
                failConfiguredObjects?.tryExecute(binder, textUpdater)
                return
            }
        }
        configuredObject.execute(binder, textUpdater)

    }

}