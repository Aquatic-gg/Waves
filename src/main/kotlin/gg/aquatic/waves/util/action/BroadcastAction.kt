package gg.aquatic.waves.util.action

import gg.aquatic.aquaticseries.lib.action.AbstractAction
import gg.aquatic.aquaticseries.lib.util.argument.AquaticObjectArgument
import gg.aquatic.aquaticseries.lib.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.aquaticseries.lib.util.replace
import gg.aquatic.aquaticseries.lib.util.toAquatic
import gg.aquatic.aquaticseries.lib.util.updatePAPIPlaceholders
import org.bukkit.entity.Player
import java.util.function.BiFunction

class BroadcastAction: AbstractAction<Player>() {
    override fun run(player: Player, args: Map<String, Any?>, textUpdater: BiFunction<Player, String, String>) {
        val messages = if (args["message"] != null) listOf(args["message"] as String) else args["messages"] as List<String>
        for (message in messages) {
            message.updatePAPIPlaceholders(player).toAquatic().replace(textUpdater, player).broadcast()
        }
    }

    override fun arguments(): List<AquaticObjectArgument<*>> {
        return listOf(
            PrimitiveObjectArgument("message", "", false),
            PrimitiveObjectArgument("messages", mutableListOf<String>(), false)
        )
    }
}