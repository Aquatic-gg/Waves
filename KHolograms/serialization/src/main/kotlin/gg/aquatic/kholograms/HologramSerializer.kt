package gg.aquatic.kholograms

import gg.aquatic.common.*
import gg.aquatic.common.location.LazyLocation
import gg.aquatic.common.location.toLazyLocation
import gg.aquatic.execute.checkConditions
import gg.aquatic.execute.condition.ConditionSerializer
import gg.aquatic.kholograms.line.AnimatedHologramLine
import gg.aquatic.kholograms.line.TextHologramLine
import gg.aquatic.kholograms.serialize.LineFactory
import gg.aquatic.kholograms.serialize.LineSettings
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.joml.Vector3f
import java.io.File

object HologramSerializer {

    suspend fun loadFromFolder(folder: File): Map<Hologram.Settings, List<LazyLocation>> {
        val holograms = HashMap<Hologram.Settings, List<LazyLocation>>()
        folder.deepFilesLookup { it.extension == "yml" }.forEach {
            holograms += loadFromFile(it)
        }
        return holograms
    }

    suspend fun loadFromFile(file: File): Map<Hologram.Settings, List<LazyLocation>> {
        val holograms = HashMap<Hologram.Settings, List<LazyLocation>>()
        if (!file.exists() || file.extension != "yml") {
            return holograms
        }
        val config = Config(file, AquaticCommon.plugin)
        val cfg = config.load()
        for (section in cfg.getSectionList("holograms")) {
            val hologram = loadHologram(section)
            val locations = section.getStringList("locations").map { it.toLazyLocation() }
            holograms += hologram to locations
        }
        return holograms
    }

    fun loadLine(section: ConfigurationSection, commonOptions: CommonHologramLineSettings): LineSettings? {
        val typeId = section.getString("type", "text")?.lowercase() ?: return null
        val type = LineFactory.REGISTRY[typeId] ?: return null
        return type.load(section, commonOptions)
    }

    private fun loadLines(objectList: List<*>, commonOptions: CommonHologramLineSettings): List<LineSettings> {
        val lines = ArrayList<LineSettings>()
        for (obj in objectList) {
            if (obj is String) {
                lines += TextHologramLine.Settings(
                    commonOptions.height,
                    obj.toMMComponent(),
                    100,
                    commonOptions.scale,
                    commonOptions.billboard,
                    { true },
                    true,
                    null,
                    true,
                    commonOptions.transformationDuration,
                    null,
                    commonOptions.teleportInterpolation,
                    commonOptions.translation
                )
                continue
            }

            val objSection = obj as? ConfigurationSection
                ?: if (obj is Map<*, *>) {
                    createConfigurationSectionFromMap(obj)
                } else null

            if (objSection != null) {
                loadLine(objSection, commonOptions)?.let { lines += it }
                continue
            }
            if (obj is List<*>) {
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
                                val frame = loadLine(objSection.getConfigurationSection(it.toString())!!, commonOptions)
                                    ?: continue
                                frames += it to frame
                                continue
                            }
                            val string = objSection.getString(it.toString()) ?: continue

                            frames += it to TextHologramLine.Settings(
                                commonOptions.height,
                                string.toMMComponent(),
                                100,
                                commonOptions.scale,
                                commonOptions.billboard,
                                { true },
                                true,
                                null,
                                true,
                                commonOptions.transformationDuration,
                                null,
                                commonOptions.teleportInterpolation,
                                commonOptions.translation
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
                        strings.joinToString("\n").toMMComponent(),
                        100,
                        commonOptions.scale,
                        commonOptions.billboard,
                        { true },
                        true,
                        null,
                        true,
                        commonOptions.transformationDuration,
                        null,
                        commonOptions.teleportInterpolation,
                        commonOptions.translation
                    )
                    continue
                }
                if (frames.isNotEmpty()) {
                    lines += AnimatedHologramLine.Settings(
                        frames,
                        commonOptions.height,
                        { true },
                        null
                    )
                }
            }
        }
        return lines
    }

    fun loadHologram(objectList: List<*>): Hologram.Settings {
        val commonOptions = CommonHologramLineSettings(1.0f, Display.Billboard.CENTER, 0, 0, 0.25, Vector3f(0f, 0f, 0f))
        val lines = loadLines(objectList, commonOptions)
        return Hologram.Settings(lines, { true }, 50)
    }

    fun loadHologram(section: ConfigurationSection): Hologram.Settings {
        val commonOptions = loadCommonSettings(section)

        val lineObjects = section.getList("lines") ?: emptyList<Any>()
        val lines = loadLines(lineObjects, commonOptions)

        //val lines = loadLines(section.getSectionList("lines"))
        val conditions = ConditionSerializer.fromSections<Player>(section.getSectionList("view-requirements"))
        val viewDistance = section.getInt("view-distance", 100)
        return Hologram.Settings(lines, { p -> conditions.checkConditions(p) }, viewDistance)
    }

    fun loadCommonSettings(section: ConfigurationSection): CommonHologramLineSettings {
        val scale = section.getDouble("scale", 1.0).toFloat()
        val billboard = section.getString("billboard", "center")?.let { Display.Billboard.valueOf(it.uppercase()) }
            ?: Display.Billboard.CENTER
        val transformationDuration = section.getInt("transformation-duration", 100)
        val teleportInterpolation = section.getInt("teleport-interpolation", 100)
        val height = section.getDouble("height", 0.5)
        val translation = section.getString("translation")?.let {
            val args = it.split(";")
            Vector3f(args[0].toFloat(), args[1].toFloat(), args[2].toFloat())
        }
        return CommonHologramLineSettings(
            scale,
            billboard,
            transformationDuration,
            teleportInterpolation,
            height,
            translation ?: Vector3f(0f, 0f, 0f)
        )
    }
}
