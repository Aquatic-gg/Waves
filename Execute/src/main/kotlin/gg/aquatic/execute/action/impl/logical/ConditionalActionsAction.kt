package gg.aquatic.execute.action.impl.logical

import gg.aquatic.execute.ClassTransform
import gg.aquatic.execute.ConfiguredExecutableObject
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
        val actions = args.any("actions") as? Collection<ConfiguredExecutableObject<T, Unit>> ?: return
        val failActions = args.any("fail") as? Collection<ConfiguredExecutableObject<T, Unit>>
        val conditions = args.any("conditions") as? Collection<ConfiguredExecutableObject<T, Boolean>> ?: return

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