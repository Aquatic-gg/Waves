package gg.aquatic.waves.registry.serializer

import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.ItemHandler
import gg.aquatic.waves.registry.WavesRegistry
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.EntityType
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack

object ItemSerializer {

    inline fun <reified T : Any> fromSection(
        section: ConfigurationSection?, crossinline mapper: (ConfigurationSection, AquaticItem) -> T
    ): T? {
        val item = fromSection(section) ?: return null

        return mapper(section!!, item)
    }

    fun fromSection(
        section: ConfigurationSection?
    ): AquaticItem? {
        section ?: return null
        val material = section.getString("material", "STONE")!!
        var lore: MutableList<String>? = null
        if (section.contains("lore")) {
            lore = section.getStringList("lore")
        }
        val enchantments: MutableMap<String, Int> = HashMap()
        if (section.contains("enchants")) {
            for (str in section.getStringList("enchants")) {
                val strs = str.split(":".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                if (strs.size < 2) {
                    continue
                }
                val enchantment = strs[0]
                val level = strs[1].toInt()
                enchantments[enchantment] = level
            }
        }
        val flags: MutableList<ItemFlag> = ArrayList()
        if (section.contains("flags")) {
            for (flag in section.getStringList("flags")) {
                val itemFlag = ItemFlag.valueOf(flag.uppercase())
                flags.add(itemFlag)
            }
        }

        val itemModel = section.getString("item-model")

        val spawnerEntityType = section.getString("entity-type")?.let { EntityType.valueOf(it.uppercase()) }
        return create(
            material,
            section.getString("display-name"),
            lore,
            section.getInt("amount", 1),
            section.getInt("model-data"),
            itemModel,
            enchantments,
            flags,
            spawnerEntityType
        )
    }

    fun fromSections(sections: List<ConfigurationSection>): List<AquaticItem> {
        return sections.mapNotNull { fromSection(it) }
    }

    inline fun <reified T : Any> fromSections(
        sections: List<ConfigurationSection>,
        crossinline mapper: (ConfigurationSection, AquaticItem) -> T
    ): List<T> {
        return sections.mapNotNull { fromSection(it, mapper) }
    }

    private fun create(
        namespace: String,
        name: String?,
        description: MutableList<String>?,
        amount: Int,
        modeldata: Int,
        itemModel: String?,
        enchantments: MutableMap<String, Int>?,
        flags: MutableList<ItemFlag>?,
        spawnerEntityType: EntityType?
    ): AquaticItem? {
        val itemStack = if (namespace.contains(":")) {
            val id = namespace.split(":").first().uppercase()
            val factory = WavesRegistry.ITEM_FACTORIES[id] ?: return null
            factory.create(namespace.substring(id.length + 1))
        } else {
            ItemStack(Material.valueOf(namespace.uppercase()))
        } ?: return null

        return ItemHandler.create(
            itemStack,
            name,
            description,
            amount,
            modeldata,
            itemModel,
            enchantments,
            flags,
            spawnerEntityType
        )
    }

}