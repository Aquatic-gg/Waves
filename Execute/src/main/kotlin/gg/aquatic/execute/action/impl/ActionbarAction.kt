package gg.aquatic.execute.action.impl

import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import gg.aquatic.execute.minimessage.toMMComponent
import org.bukkit.entity.Player

object ActionbarAction : Action<Player> {

    override fun execute(binder: Player, args: ArgumentContext<Player>) {
        val message = args.string("message")!!
        binder.sendActionBar(message.toMMComponent())
    }

    override val arguments: List<ObjectArgument<*>> = arguments { primitive("message", "", true) }
}