package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ExecutableObjectHandle
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.impl.ActionsArgument
import gg.aquatic.execute.argument.impl.ConditionsArgument

class ConditionalActionsAction<T : Any>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override fun execute(
        binder: T,
        args: ArgumentContext<T>,
    ) {
        val actions = args.typed<Collection<ExecutableObjectHandle<T, Unit>>>("actions") ?: return
        val failActions = args.typed<Collection<ExecutableObjectHandle<T, Unit>>>("fail")
        val conditions = args.typed<Collection<ExecutableObjectHandle<T, Boolean>>>("conditions") ?: return

        for (condition in conditions) {
            if (!condition.execute(binder,args.updater)) {
                failActions?.forEach { it.execute(binder, args.updater) }
                return
            }
        }
        actions.forEach { it.execute(binder, args.updater) }
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        ActionsArgument("actions", listOf(), true, clazz,super.classTransforms),
        ActionsArgument("fail", listOf(), false, clazz, super.classTransforms),
        ConditionsArgument("conditions", listOf(), true, clazz, super.classTransforms)
    )
}