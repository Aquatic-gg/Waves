package gg.aquatic.execute

import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument

class ExecutableObjectBundle<T,U>(
    val objects: List<ExecutableObject<T, U>>
): ExecutableObject<T, U> {
    override fun execute(binder: T, args: ArgumentContext<T>): U {
        var value: U? = null
        for (executableObject in objects) {
            value = executableObject.execute(binder, args)
        }
        return value!!
    }

    override val arguments: List<ObjectArgument<*>>
        get() = objects.flatMap { it.arguments }
}