package gg.aquatic.waves.registry.serializer
/*
import gg.aquatic.aquaticseries.lib.betterhologram.AquaticHologram
import gg.aquatic.aquaticseries.lib.betterhologram.impl.ArmorstandLine
import gg.aquatic.waves.hologram.impl.EmptyLine
import gg.aquatic.waves.hologram.impl.ItemDisplayLine
import gg.aquatic.aquaticseries.lib.betterhologram.impl.TextDisplayLine
import gg.aquatic.waves.util.requirement.ConfiguredRequirement
import gg.aquatic.aquaticseries.lib.util.getSectionList
import gg.aquatic.aquaticseries.lib.util.toAquatic
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.item.loadFromYml
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Function

object HologramSerializer {

    fun load(sections: Collection<ConfigurationSection>): MutableList<AquaticHologram.Line> {
        val list = mutableListOf<AquaticHologram.Line>()
        for (section in sections) {
            val line = loadLine(section) ?: continue
            list += line
        }
        return list
    }

    fun loadLine(section: ConfigurationSection): AquaticHologram.Line? {
        val type = section.getString("type") ?: return null
        when (type.lowercase()) {
            "text_display" -> return loadTextLine(section)
            "item_display" -> return loadItemLine(section)
            "armorstand" -> return loadArmorstandLine(section)
            "empty" -> return loadEmptyLine(section)
        }
        return null
    }

    fun loadTextLine(section: ConfigurationSection): TextDisplayLine {
        val failLine = loadFailLine(section)
        val requirements = loadRequirements(section)

        val keyframes = TreeMap<Int, TextDisplayLine.TextDisplayKeyframe>()
        if (!section.contains("frames")) {
            keyframes += 0 to loadTextKeyframe(section)
        } else {
            val frames = section.getConfigurationSection("frames")!!
            frames.getKeys(false).forEach {
                keyframes[it.toInt()] = loadTextKeyframe(frames.getConfigurationSection(it)!!)
            }
        }

        return TextDisplayLine(
            Function { p ->
                for (requirement in requirements) {
                    if (!requirement.check(p)) return@Function false
                }
                return@Function true
            },
            failLine,
            keyframes
        ) { p, line -> line }
    }

    private fun loadTextKeyframe(section: ConfigurationSection): TextDisplayLine.TextDisplayKeyframe {
        val text = section.getString("text")!!
        val height = section.getDouble("height", 0.3)
        val scale = section.getDouble("scale", 1.0).toFloat()
        val billboard = Billboard.valueOf(section.getString("billboard", "CENTER")!!)
        return TextDisplayLine.TextDisplayKeyframe(
            text.toAquatic(),
            height,
            scale,
            billboard
        )
    }

    fun loadItemLine(section: ConfigurationSection): ItemDisplayLine? {
        val failLine = loadFailLine(section)
        val requirements = loadRequirements(section)

        val keyframes = TreeMap<Int, ItemDisplayLine.ItemDisplayKeyframe>()
        if (!section.contains("frames")) {
            keyframes += 0 to (loadItemKeyframe(section) ?: return null)
        } else {
            val frames = section.getConfigurationSection("frames")!!
            val keys = frames.getKeys(false)
            Bukkit.getConsoleSender().sendMessage("Keys size: ${keys.size}")
            frames.getKeys(false).forEach {
                Bukkit.getConsoleSender().sendMessage("Loading frame $it")
                val loaded = loadItemKeyframe(frames.getConfigurationSection(it)!!)
                if (loaded != null) {
                    Bukkit.getConsoleSender().sendMessage("Loaded item keyframe at $it")
                    keyframes[it.toInt()] = loaded
                } else {
                    Bukkit.getConsoleSender().sendMessage("Keyframe at $it is null")
                }
            }
        }

        return ItemDisplayLine(
            Function { p ->
                for (requirement in requirements) {
                    if (!requirement.check(p)) return@Function false
                }
                return@Function true
            },
            failLine,
            keyframes
        )
    }

    private fun loadItemKeyframe(section: ConfigurationSection): ItemDisplayLine.ItemDisplayKeyframe? {
        val item = AquaticItem.loadFromYml(section) ?: return null
        val height = section.getDouble("height", 0.3)
        val scale = section.getDouble("scale", 1.0).toFloat()
        val billboard = Billboard.valueOf(section.getString("billboard", "CENTER")!!)
        val itemDisplayTransform: ItemDisplayTransform = ItemDisplayTransform.valueOf(section.getString("item-display-transform", "GROUND")!!)
        return ItemDisplayLine.ItemDisplayKeyframe(
            item.getItem(),
            height,
            scale,
            billboard,
            itemDisplayTransform
        )
    }

    fun loadArmorstandLine(section: ConfigurationSection): ArmorstandLine {
        val failLine = loadFailLine(section)
        val requirements = loadRequirements(section)

        val keyframes = TreeMap<Int, ArmorstandLine.ArmorstandKeyframe>()
        if (!section.contains("frames")) {
            keyframes += 0 to loadArmorstandKeyframe(section)
        } else {
            val frames = section.getConfigurationSection("frames")!!
            frames.getKeys(false).forEach {
                keyframes[it.toInt()] = loadArmorstandKeyframe(frames.getConfigurationSection(it)!!)
            }
        }

        return ArmorstandLine(
            Function { p ->
                for (requirement in requirements) {
                    if (!requirement.check(p)) return@Function false
                }
                return@Function true
            },
            failLine,
            keyframes
        ) { p, line -> line }
    }

    private fun loadArmorstandKeyframe(section: ConfigurationSection): ArmorstandLine.ArmorstandKeyframe {
        val text = section.getString("text")!!
        val height = section.getDouble("height", 0.3)
        return ArmorstandLine.ArmorstandKeyframe(
            text.toAquatic(),
            height,
        )
    }

    fun loadEmptyLine(section: ConfigurationSection): EmptyLine {
        val height = section.getDouble("height", 0.3)
        val failLine = loadFailLine(section)
        val requirements = loadRequirements(section)
        return EmptyLine(
            Function { p ->
                for (requirement in requirements) {
                    if (!requirement.check(p)) return@Function false
                }
                return@Function true
            },
            failLine,
            height
        )
    }

    private fun loadRequirements(section: ConfigurationSection): List<ConfiguredRequirement<Player>> {
        return if (section.contains("conditions")) {
            RequirementSerializer.fromSections(section.getSectionList("conditions"))
        } else {
            arrayListOf()
        }
    }

    private fun loadFailLine(section: ConfigurationSection): AquaticHologram.Line? {
        return if (section.isConfigurationSection("fail-line")) {
            loadLine(section.getConfigurationSection("fail-line")!!)
        } else {
            null
        }
    }

}
 */