package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import org.bukkit.entity.Player

@RegisterAction("stop-sound")
class SoundStopAction : Action<Player> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "example", true),
    )

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val sound = args.string("sound") { str -> textUpdater(binder, str) } ?: return
        binder.stopSound(sound)
    }
}