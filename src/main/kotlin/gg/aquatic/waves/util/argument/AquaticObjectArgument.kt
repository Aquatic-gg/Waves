package gg.aquatic.waves.util.argument

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

abstract class AquaticObjectArgument<T>(
    val id: String, val defaultValue: T?, val required: Boolean, val aliases: Collection<String>
) {

    abstract val serializer: AbstractObjectArgumentSerializer<T?>

    fun load(section: ConfigurationSection): T? {
        val id = (aliases + id).find { section.contains(it) } ?: return defaultValue
        return serializer.load(section, id) ?: return defaultValue
    }

    companion object {
        fun loadRequirementArguments(
            section: ConfigurationSection,
            arguments: List<AquaticObjectArgument<*>>
        ): ObjectArguments {
            val args: MutableMap<String, Any?> = java.util.HashMap()

            for (argument in arguments) {
                val loaded = argument.load(section)
                if (loaded == null && argument.required) {
                    Bukkit.getConsoleSender()
                        .sendMessage("§cARGUMENT §4" + argument.id + " §cIS MISSING, PLEASE UPDATE YOUR CONFIGURATION!")
                }
                args += argument.id to loaded
            }
            return ObjectArguments(args)
        }
    }

}