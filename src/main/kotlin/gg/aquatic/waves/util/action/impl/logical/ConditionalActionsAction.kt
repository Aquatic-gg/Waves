package gg.aquatic.waves.util.action.impl.logical

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ActionsArgument
import gg.aquatic.waves.util.argument.impl.ConditionsArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject

class ConditionalActionsAction<T : Any>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override fun execute(
        binder: T,
        args: ObjectArguments,
        textUpdater: (T, String) -> String,
    ) {
        val actions = args.any("actions") as? Collection<ConfiguredExecutableObject<T, Unit>> ?: return
        val failActions = args.any("fail") as? Collection<ConfiguredExecutableObject<T, Unit>>
        val conditions = args.any("conditions") as? Collection<ConfiguredExecutableObject<T, Boolean>> ?: return

        for (condition in conditions) {
            if (!condition.execute(binder,textUpdater)) {
                failActions?.forEach { it.execute(binder, textUpdater) }
                return
            }
        }
        actions.forEach { it.execute(binder, textUpdater) }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        ActionsArgument("actions", listOf(), true, clazz,super.classTransforms),
        ActionsArgument("fail", listOf(), false, clazz, super.classTransforms),
        ConditionsArgument("conditions", listOf(), true, clazz, super.classTransforms)
    )
}