package gg.aquatic.execute.action.impl

import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.argument.impl.PrimitiveObjectArgument
import org.bukkit.entity.Player

object SoundStopAction : Action<Player> {
    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "example", true),
    )

    override fun execute(binder: Player, args: ArgumentContext<Player>) {
        val sound = args.string("sound") ?: return
        binder.stopSound(sound)
    }
}