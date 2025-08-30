package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key

@RegisterAction("variable")
class VariableAction: Action<Scenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "example", true),
        PrimitiveObjectArgument("value", "example", true)
    )

    override fun execute(
        binder: Scenario,
        args: ObjectArguments,
        textUpdater: (Scenario, String) -> String
    ) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val value = args.string("value") { textUpdater(binder, it) } ?: return
        binder.extraPlaceholders[Key.key("variable:$id")] = { str -> str.replace("%variable:$id%", value) }
    }
}