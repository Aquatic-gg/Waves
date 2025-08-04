package gg.aquatic.waves.util.message.handler

import gg.aquatic.waves.message.MessageSerializer
import gg.aquatic.waves.util.message.Message
import org.bukkit.configuration.file.FileConfiguration

interface CfgMessageHandler: MessageHandler {
    val path: String
    val def: Any

    val config: FileConfiguration

    override val message: Message
        get() {
            val cfg = config
            return MessageSerializer.loadMessageInstance(cfg, path, def)
        }
}