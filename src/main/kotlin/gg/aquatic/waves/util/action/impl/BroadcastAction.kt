package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.MessageArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.EmptyMessage
import org.bukkit.entity.Player

@RegisterAction("broadcast")
class BroadcastAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val messages = (args.any("message")
            ?: args.any("messages") ?: return) as Message
        messages.replace { str ->
            textUpdater(binder, str)
        }.broadcast()
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        MessageArgument("message", null, false),
        MessageArgument("messages", null, false)
    )
}