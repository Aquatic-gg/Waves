package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.toUser
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.entity.Player

class MessageAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val messages = args.stringOrCollection("message") ?: return
        val user = binder.toUser() ?: return
        for (msg in messages) {
            user.sendMessage(textUpdater(binder,msg.updatePAPIPlaceholders(binder)).toMMComponent())
        }
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("message", "", true),
    )


}