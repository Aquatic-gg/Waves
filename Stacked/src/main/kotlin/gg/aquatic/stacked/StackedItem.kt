package gg.aquatic.stacked

import com.google.common.collect.HashMultimap
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.ItemOptions
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.collections.iterator

class StackedItem(
    val factoryId: String?,
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
        return item.clone()
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
        fun loadFromYml(section: ConfigurationSection?): StackedItem? {
            return ItemSerializer.fromSection(section)
        }

        val ITEM_REGISTRY_KEY = RegistryKey<String, StackedItem>(
            RegistryId("aquatic", "items")
        )

        val ITEMS: FrozenRegistry<String, StackedItem>
            get() {
                return Registry[ITEM_REGISTRY_KEY]
            }

        val ITEM_FACTORY_REGISTRY_KEY = RegistryKey<String, ItemHandler.Factory>(
            RegistryId("aquatic", "item_factories")
        )

        val ITEM_FACTORIES: FrozenRegistry<String, ItemHandler.Factory>
            get() {
                return Registry[ITEM_FACTORY_REGISTRY_KEY]
            }
    }
}