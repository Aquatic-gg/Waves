package gg.aquatic.stacked.impl

import com.google.common.collect.HashMultimap
import gg.aquatic.stacked.ItemHandler
import gg.aquatic.stacked.StackedItem
import gg.aquatic.stacked.option.ItemOptionHandle
import gg.aquatic.stacked.serialize.ItemSerializerImpl
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.inventory.ItemStack

open class StackedItemImpl(
    private val item: ItemStack,
    options: Collection<ItemOptionHandle>,
    override val handler: ItemHandler<StackedItemImpl, ItemSerializerImpl>
) : StackedItem<StackedItemImpl> {

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

        options.values.forEach { it.apply(im) }
        iS.itemMeta = im
        options.values.forEach { it.apply(iS) }
        return iS
    }

    companion object {
        fun loadFromYml(section: ConfigurationSection?): StackedItemImpl? {
            return ItemHandler.Impl.serializer.fromSection(section)
        }
    }
}