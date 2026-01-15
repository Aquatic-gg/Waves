package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.message.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PaperMessage private constructor(
    val lines: Iterable<MessageLine>,
    val pagination: PaginationSettings? = null,
    val view: MessageView = MessageView.Chat,
    private val hooks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()
) : Message<PaperMessage> {

    companion object {
        fun of(
            vararg lines: Component,
            pagination: PaginationSettings? = null,
            view: MessageView = MessageView.Chat,
            callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()
        ) = PaperMessage(
            lines.map { MessageLine(it, it.findPlaceholders()) },
            pagination,
            view,
            callbacks
        )

        fun of(
            lines: Iterable<Component>,
            pagination: PaginationSettings? = null,
            view: MessageView = MessageView.Chat,
            callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()
        ) = PaperMessage(lines.map { MessageLine(it, it.findPlaceholders()) }, pagination, view, callbacks)
    }

    class MessageLine(
        val component: Component,
        val placeholders: Set<String>
    )

    // Pre-calculate if this message is static and cache the joined component
    private val isStatic: Boolean = lines.all { it.placeholders.isEmpty() }
    private val cachedComponent: Component? = if (isStatic) {
        Component.join(JoinConfiguration.newlines(), lines.map { it.component })
    } else null

    fun withCallback(callback: (CommandSender, PaperMessage) -> Unit) = PaperMessage(lines, hooks + callback)

    fun send(vararg receiver: CommandSender) = send(receiver.asIterable())

    fun send(receivers: Iterable<CommandSender>) {
        // If static, use the pre-rendered component
        val componentToSend = cachedComponent ?: Component.join(
            JoinConfiguration.newlines(),
            lines.map { it.component }
        )

        for (player in receivers) {
            hooks.forEach { it(player, this) }
            player.sendMessage(componentToSend)
        }
    }

    private fun sendPaginated(receivers: Iterable<CommandSender>, page: Int = 0) {
        val pagination = pagination ?: return send(receivers)
        if (lines.count() == 0) {
            return
        }
        val startIndex = page * pagination.pageSize
        val endIndex = startIndex + pagination.pageSize

        if (startIndex >= lines.count()) {
            return
        }

        for (sender in receivers) {
            val components = ArrayList<Component>()

            if (pagination.header != null) {
                components.add(pagination.header.updatePaginationPlaceholders(page, sender))
            }
            for (i in startIndex until endIndex) {
                if (i >= lines.count()) {
                    break
                }
                components.add(lines.elementAt(i).component.updatePaginationPlaceholders(page, sender))
            }
            if (pagination.footer != null) {
                components.add(pagination.footer.updatePaginationPlaceholders(page, sender))
            }

            val component = Component.join(JoinConfiguration.newlines(), components)
            sender.sendMessage(component)
        }


    }

    override fun replace(
        placeholder: String,
        replacement: String
    ): PaperMessage {
        return replace(mapOf(placeholder to replacement))
    }

    fun replace(
        replacements: Map<String, String>
    ): PaperMessage {
        if (isStatic) return this
        return of(lines.map { line ->
            val filtered = replacements.filter { it.key in line.placeholders }
            if (filtered.isEmpty()) return@map line.component
            line.component.replacePlaceholders(filtered)
        }, pagination, view, hooks)
    }

    override fun replace(updater: (String) -> String): PaperMessage {
        if (isStatic) return this
        return of(lines.map {
            if (it.placeholders.isEmpty()) return@map it.component
            it.component.replacePlaceholders(updater)
        }, pagination, view, hooks)
    }

    fun replace(placeholder: String, component: Component): PaperMessage {
        if (isStatic) return this
        return of(lines.map { line ->
            if (!line.placeholders.contains(placeholder)) return@map line.component
            line.component.replaceWith(mapOf(placeholder to { component }))
        }, pagination, view, hooks)
    }

    private fun Component.updatePaginationPlaceholders(page: Int, sender: CommandSender) = replacePlaceholders(
        mapOf(
            "%aq-player%" to if (sender is Player) sender.name else "*console",
            "%aq-page%" to page.toString(),
            "%aq-prev-page%" to max((page - 1), 0).toString(),
            "%aq-next-page%" to min(
                (ceil(lines.count().toDouble() / lines.count().toDouble()).toInt() - 1),
                page + 1
            ).toString()
        )
    )
}