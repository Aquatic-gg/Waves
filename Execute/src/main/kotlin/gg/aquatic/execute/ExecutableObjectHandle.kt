package gg.aquatic.execute

import gg.aquatic.execute.argument.ObjectArguments

open class ExecutableObjectHandle<B, C>(
    val executableObject: ExecutableObject<B, C>,
    val arguments: ObjectArguments
) {

    open fun execute(binder: B, textUpdater: (B, String) -> String): C {
        val context = arguments.context(binder, textUpdater)
        return executableObject.execute(binder, context)
    }
}