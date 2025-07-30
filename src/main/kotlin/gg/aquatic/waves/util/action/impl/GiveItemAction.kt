package gg.aquatic.waves.util.action.impl

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ItemObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.item.toCustomItem
import org.bukkit.Material
import org.bukkit.entity.Player

@RegisterAction("giveitem")
class GiveItemAction: Action<Player> {
    override fun execute(binder: Player, args: ObjectArguments, textUpdater: (Player, String) -> String) {
        val customItem = args.typed<AquaticItem>("item") ?: return
        customItem.giveItem(binder)
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        ItemObjectArgument("item", Material.STONE.toCustomItem(), true)
    )
}