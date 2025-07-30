package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import org.bukkit.entity.Player

@RegisterAction("sound")
class SoundAction : Action<Player> {
    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val sound = args.string("sound") { str -> textUpdater(binder, str) } ?: return
        val volume = args.float("volume") { str -> textUpdater(binder, str) } ?: return
        val pitch = args.float("pitch") { str -> textUpdater(binder, str) } ?: return

        binder.playSound(binder.location, sound, volume, pitch)
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "minecraft:ambient.basalt_deltas.additions", true),
        PrimitiveObjectArgument("volume", 1.0f, false),
        PrimitiveObjectArgument("pitch", 1.0f, false)
    )
}