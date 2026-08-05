package gg.aquatic.common.argument

import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection

abstract class ObjectArgument<T>(
    val id: String, val defaultValue: T?, val required: Boolean, val aliases: Collection<String>
) {

    abstract val serializer: ObjectArgumentFactory<T?>

    fun load(section: ConfigurationSection): T? {
        val id = (aliases + id).find { section.contains(it) } ?: return defaultValue
        return serializer.load(section, id) ?: return defaultValue
    }

    companion object {
        fun load(
            section: ConfigurationSection,
            arguments: List<ObjectArgument<*>>
        ): ObjectArguments {
            val args: MutableMap<String, Any?> = HashMap()

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