package gg.aquatic.waves.util.message.impl.page

import gg.aquatic.waves.util.message.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

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
            sender.sendMessage(header.toMMComponent(page,sender))
        }
        for (i in startIndex until endIndex) {
            if (i >= messages.size) {
                break
            }
            sender.sendMessage(messages.elementAt(i).toMMComponent(page,sender))
        }
        if (footer != null) {
            sender.sendMessage(footer.toMMComponent(page,sender))
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

    private fun String.toMMComponent(page: Int, sender: CommandSender): Component {
        return MiniMessage.builder()
            .editTags { b ->
                b.tag("ccmd") { a, b ->
                    ConsoleCommandMMResolver.resolve(a, b)
                }
            }.build().deserialize(
                this
                    .replace("%aq-player%", if (sender is Player) sender.name else "*console")
                    .replace("%aq-page%", page.toString())
                    .replace("%aq-prev-page%", max((page - 1), 0).toString())
                    .replace(
                        "%aq-next-page%",
                        min((ceil(messages.size.toDouble() / pageSize.toDouble()).toInt() - 1), page + 1).toString()
                    )
            )
    }
}