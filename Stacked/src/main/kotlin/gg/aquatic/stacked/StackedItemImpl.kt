package gg.aquatic.stacked

import com.google.common.collect.HashMultimap
import gg.aquatic.kregistry.FrozenRegistry
import gg.aquatic.kregistry.Registry
import gg.aquatic.kregistry.RegistryId
import gg.aquatic.kregistry.RegistryKey
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.option.ItemOptions
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface StackedItem {
    val options: MutableMap<Key, ItemOptionHandle>

    fun getBaseItem(): ItemStack
    fun getItem(): ItemStack

    fun getOption(key: Key): ItemOptionHandle? {
        return options[key]
    }

    fun getOption(option: ItemOptions): ItemOptionHandle? {
        return options[option.key]
    }

    fun giveItem(player: Player, amount: Int? = null) {
        val iS = getItem()
        amount?.let { iS.amount = it }
        player.inventory.addItem(iS)
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

open class StackedItemImpl(
    private val item: ItemStack,
    options: Collection<ItemOptionHandle>
) : StackedItem {

    override val options = options.associateBy { it.key }.toMutableMap()

    override fun getBaseItem(): ItemStack {
        return item.clone()
    }

    override fun getItem(): ItemStack {
        val iS = getBaseItem()

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
}