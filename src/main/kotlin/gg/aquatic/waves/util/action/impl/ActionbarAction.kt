package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.entity.Player

class ActionbarAction : Action<Player> {

    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val message = ((args.string("message") { str -> textUpdater(binder, str) })!!).updatePAPIPlaceholders(binder)
        binder.sendActionBar(textUpdater(binder, message).toMMComponent())
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(PrimitiveObjectArgument("message", "", true))
}