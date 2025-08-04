package gg.aquatic.waves.message

import gg.aquatic.waves.Waves
import gg.aquatic.waves.util.Config
import gg.aquatic.waves.util.deepFilesLookup
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.Messages
import gg.aquatic.waves.util.message.impl.SimpleMessage
import gg.aquatic.waves.util.message.impl.page.PaginatedMessage
import org.bukkit.configuration.file.FileConfiguration

object MessageSerializer {

    private val previousMessages = HashSet<String>()

    fun loadWavesCustomMessages() {
        for (string in previousMessages) {
            Messages.registeredMessages.remove(string)
        }
        previousMessages.clear()
        val files = Waves.INSTANCE.dataFolder.resolve("custom-messages").apply { mkdirs() }.deepFilesLookup { it.extension == "yml" }
        for (file in files) {
            val config = Config(file, Waves.INSTANCE)
            config.load()
            loadWavesCustomMessages(config)
        }
    }

    internal fun loadWavesCustomMessages(config: Config) {
        val cfg = config.getConfiguration()!!
        for (key in cfg.getKeys(false)) {
            previousMessages += "waves:$key"
            Messages.registeredMessages["waves:$key"] = {
                loadMessageInstance(cfg,key,"")
            }
        }
    }

    fun loadMessageInstance(cfg: FileConfiguration, path: String, def: Any): Message {
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