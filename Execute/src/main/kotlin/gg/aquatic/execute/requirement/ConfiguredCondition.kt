package gg.aquatic.execute.requirement

import gg.aquatic.execute.argument.ObjectArguments
import gg.aquatic.execute.ConfiguredExecutableObject
import gg.aquatic.execute.ExecutableObject

class ConfiguredCondition<A>(executableObject: ExecutableObject<A,Boolean>, arguments: ObjectArguments) : ConfiguredExecutableObject<A, Boolean>(
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