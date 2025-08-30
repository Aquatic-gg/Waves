package gg.aquatic.waves.scenario.action.path

import gg.aquatic.waves.scenario.prop.path.PathPoint
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import java.util.TreeMap

class PathPointsArgument(
    id: String,
    defaultValue: TreeMap<Int, PathPoint>?, required: Boolean, aliases: Collection<String> = listOf(),
) : AquaticObjectArgument<TreeMap<Int, PathPoint>>(id, defaultValue, required, aliases) {

    override val serializer: AbstractObjectArgumentSerializer<TreeMap<Int, PathPoint>?> = Serializer

    object Serializer : AbstractObjectArgumentSerializer<TreeMap<Int, PathPoint>?>() {
        override fun load(section: ConfigurationSection, id: String): TreeMap<Int, PathPoint> {
            val map = TreeMap<Int, PathPoint>()
            val s = section.getConfigurationSection(id) ?: return map
            Bukkit.getConsoleSender().sendMessage("Points path: ${s.currentPath}")
            for (key in s.getKeys(false)) {
                val pointSection = s.getConfigurationSection(key) ?: continue
                Bukkit.getConsoleSender().sendMessage("Point path: ${pointSection.currentPath}")
                val delay = key.toIntOrNull() ?: continue
                val x = pointSection.getDouble("x")
                val y = pointSection.getDouble("y")
                val z = pointSection.getDouble("z")
                val yaw = pointSection.getDouble("yaw").toFloat()
                val pitch = pointSection.getDouble("pitch").toFloat()
                map += delay to PathPoint(x, y, z, yaw, pitch)
            }
            return map
        }
    }
}