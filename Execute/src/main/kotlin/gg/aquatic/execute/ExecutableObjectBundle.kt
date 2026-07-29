package gg.aquatic.execute

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument

class ExecutableObjectBundle<T,U>(
    val objects: List<ExecutableObject<T, U>>
): ExecutableObject<T, U> {

    override val binder: Class<out T>
        get() = TODO("Not yet implemented")

    override suspend fun execute(binder: T, args: ArgumentContext<T>): U {
        var value: U? = null
        for (executableObject in objects) {
            value = executableObject.execute(binder, args)
        }
        return value!!
    }

    override val arguments: List<ObjectArgument<*>>
        get() = objects.flatMap { it.arguments }
}