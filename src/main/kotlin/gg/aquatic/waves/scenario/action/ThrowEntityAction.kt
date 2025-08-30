package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.prop.Throwable
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key

@RegisterAction("throw-entity")
class ThrowEntityAction : Action<Scenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("velocity", "0;0;0", false),
        PrimitiveObjectArgument("power", 1.0, false),
        PrimitiveObjectArgument("prop", "entity:example", true)
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val velocity = args.vector("velocity") { textUpdater(binder, it) } ?: return
        val power = args.double("power") { textUpdater(binder, it) } ?: 0.0
        val property = args.string("prop") { textUpdater(binder, it) } ?: "entity:example"

        val prop = binder.props[Key.key(property)] ?: return
        if (prop !is Throwable) return

        prop.throwObject(velocity.clone().multiply(power))
    }
}