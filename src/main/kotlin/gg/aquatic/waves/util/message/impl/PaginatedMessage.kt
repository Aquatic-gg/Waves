package gg.aquatic.waves.util.message.impl

import gg.aquatic.waves.util.message.Message
import gg.aquatic.waves.util.toMMComponent
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class PaginatedMessage(
    override var messages: Collection<String>,
    val pageSize: Int = 10,
    val header: String? = null,
    val footer: String? = null,
) : Message {
    override fun replace(updater: (String) -> String): PaginatedMessage {
        this.messages = messages.map { updater(it) }.toMutableList()
        return this
    }

    override fun replace(from: String, to: String): PaginatedMessage {
        messages = messages.map { it.replace(from, to) }.toMutableList()
        return this
    }

    fun send(sender: CommandSender, page: Int) {
        if (messages.isEmpty()) {
            return
        }
        if (messages.size == 1 && messages.first().isEmpty()) {
            return
        }
        val startIndex = page * pageSize
        val endIndex = startIndex + pageSize

        if (startIndex >= messages.size) {
            return
        }

        if (header != null) {
            sender.sendMessage(header.toMMComponent())
        }
        for (i in startIndex until endIndex) {
            if (i >= messages.size) {
                break
            }
            sender.sendMessage(messages.elementAt(i).toMMComponent())
        }
        if (footer != null) {
            sender.sendMessage(footer.toMMComponent())
        }
    }

    override fun send(sender: CommandSender) {
        send(sender, 0)
    }

    override fun broadcast() {
        broadcast(0)
    }

    fun broadcast(page: Int) {
        if (messages.size == 1 && messages.first().isEmpty() || messages.isEmpty()) {
            return
        }
        for (onlinePlayer in Bukkit.getOnlinePlayers()) {
            send(onlinePlayer, page)
        }
    }
}