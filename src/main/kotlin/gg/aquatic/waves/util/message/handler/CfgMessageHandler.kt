package gg.aquatic.waves.util.message.handler

import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.page.PaginatedMessage
import gg.aquatic.waves.util.message.impl.SimpleMessage
import org.bukkit.configuration.file.FileConfiguration

interface CfgMessageHandler: MessageHandler {
    val path: String
    val def: Any

    val config: FileConfiguration

    override val message: Message
        get() {
            val cfg = config
            val value = cfg.get(path, def)
            return if (cfg.isString(path)) {
                return SimpleMessage(value as String)
            } else if (cfg.isList(path)) {
                SimpleMessage((value as List<*>).map { it.toString() })
            } else {
                val section = cfg.getConfigurationSection(path)
                if (section == null) {
                    if (value is List<*>) {
                        return SimpleMessage(value as List<String>)
                    }
                    return SimpleMessage(value.toString())
                }
                val isPaginated = section.getBoolean("paginated", false)

                if (isPaginated) {
                    val messages = section.getStringList("messages")
                    val pageSize = section.getInt("page-size", 10)
                    val header = section.getString("header")
                    val footer = section.getString("footer")

                    PaginatedMessage(messages,pageSize,header,footer)
                } else SimpleMessage(emptyList())
            }
        }
}