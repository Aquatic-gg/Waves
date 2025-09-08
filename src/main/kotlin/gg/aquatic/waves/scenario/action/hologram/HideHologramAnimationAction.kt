package gg.aquatic.waves.scenario.action.hologram

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key

@RegisterAction("hide-hologram")
class HideHologramAnimationAction: Action<Scenario> {
    override fun execute(
        binder: Scenario,
        args: ObjectArguments,
        textUpdater: (Scenario, String) -> String,
    ) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        binder.props.remove(Key.key("hologram:$id"))?.onEnd() ?: return
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "example", true),
    )
}