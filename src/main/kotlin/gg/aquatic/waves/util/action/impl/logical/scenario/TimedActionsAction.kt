package gg.aquatic.waves.util.action.impl.logical.scenario

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.prop.timer.TimedActionsAnimationProp
import gg.aquatic.waves.util.action.impl.logical.SmartAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.TimedActionsArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import net.kyori.adventure.key.Key
import java.util.*

class TimedActionsAction<T : Scenario>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        TimedActionsArgument(
            "actions",
            HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>>(),
            true,
            clazz,
            super.classTransforms
        ),
    )

    @Suppress("UNCHECKED_CAST")
    override fun execute(binder: T, args: ObjectArguments, textUpdater: (T, String) -> String) {
        val actions = args.any("actions") as? HashMap<Int, Collection<ConfiguredExecutableObject<T, Unit>>> ?: return

        val prop = TimedActionsAnimationProp(binder, actions)
        binder.props[Key.key("timed-actions:${UUID.randomUUID()}")] = prop
        prop.tick()
    }


}