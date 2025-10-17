package gg.aquatic.waves.util.action.impl.logical.scenario

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.prop.timer.TickerAnimationProp
import gg.aquatic.waves.util.action.impl.logical.SmartAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ActionsArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.ClassTransform
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import net.kyori.adventure.key.Key

class StartTickerAction<T : Scenario>(
    clazz: Class<T>,
    classTransforms: Collection<ClassTransform<*, *>>,
) : SmartAction<T>(clazz, classTransforms as Collection<ClassTransform<T, *>>) {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        ActionsArgument("actions", listOf(), true, clazz, this.classTransforms),
        PrimitiveObjectArgument("tick-every", 1, false),
        PrimitiveObjectArgument("id", "example", false),
        PrimitiveObjectArgument("repeat-limit", -1, false)
    )

    override fun execute(binder: T, args: ObjectArguments, textUpdater: (T, String) -> String) {
        val actions = args.any("actions") as? Collection<ConfiguredExecutableObject<T, Unit>> ?: return
        val tickEvery = args.int("tick-every") { textUpdater(binder, it) } ?: return
        val repeatLimit = args.int("repeat-limit") { textUpdater(binder, it)} ?: -1
        val id = args.string("id") { textUpdater(binder, it) } ?: return

        if (actions.isEmpty()) {
            println("Ticker actions are empty, skipping")
            return
        }

        val prop = TickerAnimationProp(binder, id, tickEvery, actions, repeatLimit)
        binder.props[Key.key("ticker:$id")] = prop
    }
}