package gg.aquatic.waves.util.message.impl

import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import java.util.function.Consumer

class SimpleMessage(override var messages: Collection<String>): Message {

    constructor(message: String?): this(message?.let { mutableListOf(it) } ?: mutableListOf())

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
        }
    }

    override fun broadcast() {
        if (messages.size == 1 && messages.first().isEmpty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            messages.forEach(Consumer { v: String ->
                onlinePlayer.sendMessage(v.toMMComponent())
            })
        }

    }

}