package gg.aquatic.execute

import gg.aquatic.common.argument.ObjectArguments

open class ExecutableObjectHandle<B, C>(
    val executableObject: ExecutableObject<B, C>,
    val arguments: ObjectArguments
) {

    open suspend fun execute(binder: B, textUpdater: (B, String) -> String): C {
        val context = arguments.context(binder, textUpdater)
        return executableObject.execute(binder, context)
    }
}