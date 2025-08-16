package gg.aquatic.waves.util.message.impl

import gg.aquatic.waves.util.collection.executeActions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.page.ConsoleCommandMMResolver
import gg.aquatic.waves.util.updatePAPIPlaceholders
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.function.Consumer

class SimpleMessage(
    override var messages: Collection<String>,
    val actions: Collection<ConfiguredExecutableObject<Player, Unit>> = emptyList(),
) : Message {

    constructor(message: String?) : this(message?.let { mutableListOf(it) } ?: mutableListOf())

    override fun replace(updater: (String) -> String): SimpleMessage {
        this.messages = messages.map { updater(it) }.toMutableList()
        return this
    }

    override fun replace(from: String, to: String): SimpleMessage {
        messages = messages.map { it.replace(from, to) }.toMutableList()
        return this
    }

    override fun send(sender: CommandSender) {
        for (string in messages) {
            sender.sendMessage(string.toMMComponent())
            if (sender is Player) {
                actions.executeActions(sender) { _, str -> str.updatePAPIPlaceholders(sender) }
            }
        }
    }

    override fun broadcast() {
        if (messages.size == 1 && messages.first().isEmpty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            messages.forEach(Consumer { v: String ->
                onlinePlayer.sendMessage(v.toMMComponent())
                actions.executeActions(onlinePlayer) { _, str -> str.updatePAPIPlaceholders(onlinePlayer) }
            })
        }

    }

    private fun String.toMMComponent(): Component {
        return MiniMessage.builder()
            .editTags { b ->
                b.tag("ccmd") { a, b ->
                    ConsoleCommandMMResolver.resolve(a, b)
                }
            }.build().deserialize(
                this
            )
    }

}