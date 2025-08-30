package gg.aquatic.waves.util.action.impl.logical.scenario

import gg.aquatic.waves.scenario.prop.timer.LaterActionsAnimationProp
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.action.impl.logical.SmartAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ActionsArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import net.kyori.adventure.key.Key
import java.util.*

@RegisterAction("delayed-actions")
class LaterActionsAction<T : Scenario>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("delay", 0, true),
        ActionsArgument("actions", listOf(), true, clazz,super.classTransforms),
    )

    override fun execute(
        binder: T,
        args: ObjectArguments,
        textUpdater: (T, String) -> String,
    ) {
        val delay = args.int("delay") { textUpdater(binder, it) } ?: return
        val actions = args.any("actions") as? Collection<ConfiguredExecutableObject<T, Unit>> ?: return
        val prop = LaterActionsAnimationProp(
            binder,
            actions,
            delay
        )
        binder.props[Key.key("later-actions:${UUID.randomUUID()}")] = prop
    }
}