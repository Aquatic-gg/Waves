package gg.aquatic.execute.requirement

import gg.aquatic.execute.argument.ObjectArguments
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.ExecutableObject

class ConditionHandle<A>(executableObject: ExecutableObject<A,Boolean>, arguments: ObjectArguments) : ExecutableObjectHandle<A, Boolean>(
    executableObject, arguments
) {

    override fun execute(binder: A, textUpdater: (A, String) -> String): Boolean {
        val negate = arguments.boolean("negate") { str -> textUpdater(binder, str) } ?: false
        val context = arguments.context(binder, textUpdater)
        val value = executableObject.execute(binder, context)
        if (negate) {
            return !value
        }
        return value
    }

}