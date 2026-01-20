package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.execute.arguments.ActionsArgument
import gg.aquatic.execute.arguments.ConditionsArgument

@Suppress("UNCHECKED_CAST")
class ConditionalActionsAction<T : Any>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override suspend fun execute(
        binder: T,
        args: ArgumentContext<T>,
    ) {
        val actions = args.any("actions") as? Collection<ExecutableObjectHandle<T, Unit>> ?: return
        val failActions = args.any("fail") as? Collection<ExecutableObjectHandle<T, Unit>>
        val conditions = args.any("conditions") as? Collection<ExecutableObjectHandle<T, Boolean>> ?: return

        for (condition in conditions) {
            if (!condition.execute(binder) { _, str -> args.updater(binder, str) }) {
                failActions?.forEach { it.execute(binder) { _, str -> args.updater(binder, str) } }
                return
            }
        }
        actions.forEach { it.execute(binder) { _, str -> args.updater(binder, str) } }
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        ActionsArgument("actions", listOf(), true, clazz, super.classTransforms),
        ActionsArgument("fail", listOf(), false, clazz, super.classTransforms),
        ConditionsArgument("conditions", listOf(), true, clazz, super.classTransforms)
    )
}