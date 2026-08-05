package gg.aquatic.execute.action.impl

import gg.aquatic.common.AquaticCommon
import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.execute.action.type.PlayerAction
import org.bukkit.entity.Player

object ActionbarAction : PlayerAction() {

    override suspend fun execute(binder: Player, args: ArgumentContext<Player>) {
        val message = args.string("message") ?: return
        binder.sendActionBar(AquaticCommon.miniMessage.parse(message))
    }

    override val arguments: List<ObjectArgument<*>> = arguments { primitive("message", "", true) }
}