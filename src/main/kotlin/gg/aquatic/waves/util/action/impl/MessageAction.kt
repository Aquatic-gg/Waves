package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.MessageArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.EmptyMessage
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.entity.Player

@RegisterAction("message")
class MessageAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val messages = args.any("message") as? Message ?: return
        messages.replace { str ->
            textUpdater(binder, str)
        }.send(binder)
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        MessageArgument("message", EmptyMessage(), true),
    )


}