package gg.aquatic.waves.hologram

import gg.aquatic.waves.hologram.line.AnimatedHologramLine
import gg.aquatic.waves.hologram.line.TextHologramLine
import gg.aquatic.waves.hologram.serialize.LineSettings
import gg.aquatic.waves.registry.WavesRegistry
import gg.aquatic.waves.registry.serializer.RequirementSerializer
import gg.aquatic.waves.util.createConfigurationSectionFromMap
import gg.aquatic.waves.util.getSectionList
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player

object HologramSerializer {

    fun loadLine(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
        val typeId = section.getString("type", "text")?.lowercase() ?: return null
        val type = WavesRegistry.HOLOGRAM_LINE_FACTORIES[typeId] ?: return null

        return type.load(section, commonOptions )
    }

    private fun loadLines(objectList: List<*>, commonOptions: CommonHologramLineSettings): List<LineSettings> {
        val lines = ArrayList<LineSettings>()

        println("Loading lines... ${objectList.size} objects")
        for (obj in objectList) {
            if (obj is String) {
                println("It is string!")
                lines += TextHologramLine.Settings(
                    commonOptions.height,
                    obj,
                    100,
                    commonOptions.scale,
                    commonOptions.billboard,
                    listOf(),
                    true,
                    null,
                    true,
                    commonOptions.transformationDuration,
                    null,
                    commonOptions.teleportInterpolation
                )
                continue
            }

            val objSection = obj as? ConfigurationSection
                ?: if (obj is Map<*, *>) {
                    createConfigurationSectionFromMap(obj)
                } else null

            if (objSection != null) {
                println("It is object!")
                loadLine(objSection, commonOptions)?.let { lines += it }
                continue
            }
            if (obj is List<*>) {
                println("It is list!")
                val strings = ArrayList<String>()
                val frames = ArrayList<Pair<Int, LineSettings>>()
                for (any in obj) {
                    val objSection = any as? ConfigurationSection
                        ?: if (any is Map<*, *>) {
                            createConfigurationSectionFromMap(any)
                        } else null

                    if (objSection != null) {
                        objSection.getKeys(false).first().toIntOrNull()?.let {
                            if (objSection.isConfigurationSection(it.toString())) {
                                val frame = loadLine(objSection.getConfigurationSection(it.toString())!!, commonOptions) ?: continue
                                frames += it to frame
                                continue
                            }
                            val string = objSection.getString(it.toString()) ?: continue

                            frames += it to TextHologramLine.Settings(
                                commonOptions.height,
                                string,
                                100,
                                commonOptions.scale,
                                commonOptions.billboard,
                                listOf(),
                                true,
                                null,
                                true,
                                commonOptions.transformationDuration,
                                null,
                                commonOptions.teleportInterpolation
                            )
                        }
                        continue
                    } else if (any is String) {
                        strings += any
                    }
                }
                if (strings.isNotEmpty()) {
                    lines += TextHologramLine.Settings(
                        commonOptions.height,
                        strings.joinToString("\n"),
                        100,
                        commonOptions.scale,
                        commonOptions.billboard,
                        listOf(),
                        true,
                        null,
                        true,
                        commonOptions.transformationDuration,
                        null,
                        commonOptions.teleportInterpolation
                    )
                    continue
                }
                if (frames.isNotEmpty()) {
                    lines += AnimatedHologramLine.Settings(
                        frames,
                        commonOptions.height,
                        listOf(),
                        null
                    )
                }
            }
        }
        return lines
    }

    fun loadHologram(objectList: List<*>): AquaticHologram.Settings {
        val commonOptions = CommonHologramLineSettings(1.0f, Display.Billboard.CENTER, 0, 0, 0.25)
        val lines = loadLines(objectList, commonOptions)
        return AquaticHologram.Settings(lines, listOf(), 50)
    }

    fun loadHologram(section: ConfigurationSection): AquaticHologram.Settings {
        val commonOptions = loadCommonSettings(section)

        val lineObjects = section.getList("lines") ?: emptyList<Any>()
        val lines = loadLines(lineObjects, commonOptions)

        //val lines = loadLines(section.getSectionList("lines"))
        val conditions = RequirementSerializer.fromSections<Player>(section.getSectionList("view-requirements"))
        val viewDistance = section.getInt("view-distance", 100)
        return AquaticHologram.Settings(lines, conditions, viewDistance)
    }

    fun loadCommonSettings(section: ConfigurationSection): CommonHologramLineSettings {
        val scale = section.getDouble("scale", 1.0).toFloat()
        val billboard = section.getString("billboard", "center")?.let { Display.Billboard.valueOf(it.uppercase()) }
            ?: Display.Billboard.CENTER
        val transformationDuration = section.getInt("transformation-duration", 100)
        val teleportInterpolation = section.getInt("teleport-interpolation", 100)
        val height = section.getDouble("height", 0.5)
        return CommonHologramLineSettings(scale, billboard, transformationDuration, teleportInterpolation, height)
    }
}