package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.argument.impl.VectorArgument
import gg.aquatic.waves.util.generic.Action

@RegisterAction("push-player")
class PushPlayerAction : Action<PlayerScenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        VectorArgument("velocity", null, false),
        PrimitiveObjectArgument("power", "double", true)
    )

    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String
    ) {
        val power = args.double("power") { textUpdater(binder, it) } ?: 1.0
        val velocity = args.vector("velocity") { textUpdater(binder, it) }
        val vector = velocity?.multiply(power) ?: binder.player.location.clone()
            .subtract(binder.baseLocation).toVector().normalize()
            .multiply(power)

        binder.player.velocity = vector
    }
}