package gg.aquatic.waves.util.generic

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments

class ExecutableObjectBundle<T,U>(
    val objects: List<ExecutableObject<T,U>>
): ExecutableObject<T,U> {
    override fun execute(
        binder: T,
        args: ObjectArguments,
        textUpdater: (T, String) -> String,
    ): U {
        var value: U? = null
        for (executableObject in objects) {
            value = executableObject.execute(binder, args, textUpdater)
        }
        return value!!
    }

    override val arguments: List<AquaticObjectArgument<*>>
        get() = objects.flatMap { it.arguments }
}