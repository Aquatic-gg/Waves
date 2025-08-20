package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.generic.Action
import org.bukkit.entity.Player

@RegisterAction("close-inventory")
class CloseInventory: Action<Player> {
    override fun execute(
        binder: Player,
        args: ObjectArguments,
        textUpdater: (Player, String) -> String,
    ) {
        binder.closeInventory()
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf()
}