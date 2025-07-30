package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.broadcast
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.entity.Player

@RegisterAction("broadcast")
class BroadcastAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val messages = args.stringOrCollection("message")
            ?: args.stringOrCollection("messages") ?: return
        for (message in messages) {
            textUpdater(binder, message.updatePAPIPlaceholders(binder)).toMMComponent().broadcast()
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("message", "", false),
        PrimitiveObjectArgument("messages", mutableListOf<String>(), false)
    )
}