package gg.aquatic.waves.util.message.impl

import gg.aquatic.waves.util.collection.executeActions
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.message.impl.view.MessageView
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.updatePAPIPlaceholders
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class SimpleMessage(
    override var messages: Collection<String>,
    val actions: Collection<ConfiguredExecutableObject<Player, Unit>> = emptyList(),
    val view: MessageView = MessageView.Chat
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
        view.send(sender,messages.map { it.toMMComponent() })
        if (sender is Player) {
            actions.executeActions(sender) { _, str -> str.updatePAPIPlaceholders(sender) }
        }
    }

    override fun broadcast() {
        if (messages.size == 1 && messages.first().isEmpty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            view.send(onlinePlayer,messages.map { it.toMMComponent() })
            actions.executeActions(onlinePlayer) { _, str -> str.updatePAPIPlaceholders(onlinePlayer) }
        }

    }

}