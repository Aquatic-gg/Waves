package gg.aquatic.waves.item

import com.google.common.collect.HashMultimap
import gg.aquatic.waves.item.option.ItemOptionHandle
import gg.aquatic.waves.item.option.ItemOptions
import gg.aquatic.waves.registry.serializer.ItemSerializer
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class AquaticItem(
    val internalId: String? = null,
    private val item: ItemStack,
    options: Collection<ItemOptionHandle>
) {

    val options = options.associateBy { it.key }.toMutableMap()

    fun getOption(key: Key): ItemOptionHandle? {
        return options[key]
    }
    fun getOption(option: ItemOptions): ItemOptionHandle? {
        return options[option.key]
    }

    fun giveItem(player: Player) {
        val iS = getItem()
        player.inventory.addItem(iS)
    }

    fun giveItem(player: Player, amount: Int) {
        val iS = getItem()
        iS.amount = amount

        player.inventory.addItem(iS)
    }

    fun getUnmodifiedItem(): ItemStack {
        return item
    }

    fun getItem(): ItemStack {
        val iS = getUnmodifiedItem()

        val im = iS.itemMeta ?: return iS
        val modifiers = im.attributeModifiers
        if (modifiers == null) {
            im.attributeModifiers = HashMultimap.create(iS.type.defaultAttributeModifiers)
        }

        for (handle in options) {
            handle.value.apply(im)
        }

        iS.itemMeta = im
        for (handle in options) {
            handle.value.apply(iS)
        }
        return iS
    }

    companion object {
        fun loadFromYml(section: ConfigurationSection?): AquaticItem? {
            return ItemSerializer.fromSection(section)
        }
    }
}