package gg.aquatic.execute.action.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.Action
import org.bukkit.entity.Player

object SoundStopAction : Action<Player> {
    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "example", true),
    )

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val sound = args.string("sound") ?: return
        binder.stopSound(sound)
    }
}