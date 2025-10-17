package gg.aquatic.waves.util.argument.impl

import gg.aquatic.waves.message.MessageSerializer
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.EmptyMessage
import org.bukkit.configuration.ConfigurationSection

class MessageArgument(id: String, defaultValue: Message?, required: Boolean, aliases: Collection<String> = listOf()) :
    AquaticObjectArgument<Message>(
        id, defaultValue,
        required, aliases
    ) {
    override val serializer: AbstractObjectArgumentSerializer<Message?> = Companion

    companion object : AbstractObjectArgumentSerializer<Message?>() {
        override fun load(
            section: ConfigurationSection,
            id: String,
        ): Message? {
            val msg = MessageSerializer.loadMessageInstance(section, id, "")
            if (msg is EmptyMessage) {
                return null
            }
            return msg
        }
    }
}