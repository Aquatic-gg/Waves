package gg.aquatic.execute.action.impl

import gg.aquatic.execute.Action
import gg.aquatic.execute.argument.ArgumentContext
import gg.aquatic.execute.argument.ObjectArgument
import org.bukkit.entity.Player

object CloseInventory: Action<Player> {
    override fun execute(binder: Player, args: ArgumentContext<Player>) {
        binder.closeInventory()
    }

    override val arguments: List<ObjectArgument<*>> = listOf()
}