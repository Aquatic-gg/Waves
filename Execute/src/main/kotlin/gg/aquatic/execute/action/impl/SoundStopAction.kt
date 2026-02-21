package gg.aquatic.execute.action.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.execute.action.type.PlayerAction
import org.bukkit.entity.Player

object SoundStopAction : PlayerAction() {
    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("sound", "example", true),
    )

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val sound = args.string("sound") ?: return
        binder.stopSound(sound)
    }
}