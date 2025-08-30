package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action

@RegisterAction("stop-sound")
class StopSoundAction : Action<PlayerScenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "example", true),
    )

    override fun execute(binder: PlayerScenario, args: ObjectArguments, textUpdater: (PlayerScenario, String) -> String) {
        val sound = args.string("sound") { str -> textUpdater(binder, str) } ?: return
        binder.player.stopSound(sound)
    }
}