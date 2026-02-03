package gg.aquatic.kholograms.serialize

import gg.aquatic.common.getSectionList
import gg.aquatic.common.toMMComponent
import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.requirement.ConditionSerializer
import gg.aquatic.kholograms.CommonHologramLineSettings
import gg.aquatic.kholograms.HologramSerializer
import gg.aquatic.kholograms.line.AnimatedHologramLine
import gg.aquatic.kholograms.line.ItemHologramLine
import gg.aquatic.kholograms.line.TextHologramLine
import gg.aquatic.stacked.StackedItem
import org.bukkit.Color
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display.Billboard
import org.bukkit.entity.ItemDisplay.ItemDisplayTransform
import org.bukkit.entity.Player
import org.joml.Vector3f

object LineFactoryDefaults {
    fun registerDefaults() {
        LineFactory.REGISTRY["text"] = TextHologramLineFactory
        LineFactory.REGISTRY["item"] = ItemHologramLineFactory
        LineFactory.REGISTRY["animated"] = AnimatedHologramLineFactory
    }
}

object TextHologramLineFactory : LineFactory {
    override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
        val text = section.getString("text") ?: return null
        val height = section.getDouble("height", commonOptions.height)
        val lineWidth = section.getInt("line-width", 100)
        val scale = section.getDouble("scale", commonOptions.scale.toDouble()).toFloat()
        val billboard = section.getString("billboard")?.let {
            Billboard.valueOf(it.uppercase())
        } ?: commonOptions.billboard
        val conditions = ConditionSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
        val failLine = section.getConfigurationSection("fail-line")?.let {
            HologramSerializer.loadLine(it, commonOptions)
        }
        val hasShadow = section.getBoolean("has-shadow", false)
        val backgroundColorStr = section.getString("background-color")
        val isSeeThrough = section.getBoolean("is-see-through", true)
        val transformationDuration = section.getInt("transformation-duration", commonOptions.transformationDuration)
        val backgroundColor = if (backgroundColorStr != null) {
            val args = backgroundColorStr.split(";").map { it.toIntOrNull() ?: 0 }
            Color.fromARGB(args.getOrNull(3) ?: 255, args[0], args[1], args[2])
        } else null
        val teleportInterpolation = section.getInt("teleport-interpolation", commonOptions.teleportInterpolation)
        val translation = section.getString("translation")?.let {
            val args = it.split(";")
            Vector3f(args[0].toFloat(), args[1].toFloat(), args[2].toFloat())
        } ?: commonOptions.translation

        return TextHologramLine.Settings(
            height,
            text.toMMComponent(),
            lineWidth,
            scale,
            billboard,
            { p -> conditions.checkConditions(p) },
            hasShadow,
            backgroundColor,
            isSeeThrough,
            transformationDuration,
            failLine,
            teleportInterpolation,
            translation
        )
    }
}

object ItemHologramLineFactory : LineFactory {
    override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
        val item = StackedItem.loadFromYml(section.getConfigurationSection("item")) ?: return null
        val height = section.getDouble("height", commonOptions.height)
        val scale = section.getDouble("scale", commonOptions.scale.toDouble()).toFloat()
        val billboard = section.getString("billboard")?.let {
            Billboard.valueOf(it.uppercase())
        } ?: commonOptions.billboard
        val itemDisplayTransform =
            ItemDisplayTransform.valueOf(section.getString("item-display-transform", "NONE")!!.uppercase())
        val conditions = ConditionSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
        val failLine = section.getConfigurationSection("fail-line")?.let {
            HologramSerializer.loadLine(it, commonOptions)
        }
        val translation = section.getString("translation")?.let {
            val args = it.split(";")
            Vector3f(args[0].toFloat(), args[1].toFloat(), args[2].toFloat())
        } ?: commonOptions.translation
        val transformationDuration = section.getInt("transformation-duration", commonOptions.transformationDuration)
        val teleportInterpolation = section.getInt("teleport-interpolation", commonOptions.teleportInterpolation)
        return ItemHologramLine.Settings(
            item.getItem(),
            height,
            scale,
            billboard,
            itemDisplayTransform,
            { p -> conditions.checkConditions(p) },
            failLine,
            transformationDuration,
            teleportInterpolation,
            translation
        )
    }
}

object AnimatedHologramLineFactory : LineFactory {
    override fun load(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
        val frames = ArrayList<Pair<Int, LineSettings>>()
        val height = section.getDouble("height", commonOptions.height)
        val conditions = ConditionSerializer.fromSections<Player>(section.getSectionList("view-conditions"))
        val failLine = section.getConfigurationSection("fail-line")?.let {
            HologramSerializer.loadLine(it, commonOptions)
        }
        for (configurationSection in section.getSectionList("frames")) {
            val frame = HologramSerializer.loadLine(configurationSection, commonOptions) ?: continue
            val stay = configurationSection.getInt("stay", 1)
            frames.add(stay to frame)
        }
        if (frames.isEmpty()) return null
        return AnimatedHologramLine.Settings(
            frames,
            height,
            { p -> conditions.checkConditions(p) },
            failLine
        )
    }
}
