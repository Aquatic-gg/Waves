package gg.aquatic.execute.action.impl

import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import org.bukkit.entity.Player

object SoundAction : Action<Player> {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val sound = args.string("sound") ?: return
        val volume = args.float("volume") ?: return
        val pitch = args.float("pitch") ?: return

        binder.playSound(binder.location, sound, volume, pitch)
    }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "minecraft:ambient.basalt_deltas.additions", true),
        PrimitiveObjectArgument("volume", 1.0f, false),
        PrimitiveObjectArgument("pitch", 1.0f, false)
    )
}