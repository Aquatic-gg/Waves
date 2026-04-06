package gg.aquatic.klocale.impl.paper

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

class PaginatedPaperMessage internal constructor(
    lines: Iterable<PaperMessage.MessageLine>,
    val pagination: PaginationSettings,
    view: MessageView = MessageView.Chat,
    hooks: List<(CommandSender, PaperMessage) -> Unit> = emptyList(),
    visibilityResolver: (suspend (MessageContext, List<ComponentVisibilityPayload>) -> Boolean)? = null,
) : PaperMessage(lines, view, hooks, visibilityResolver) {

    override fun withCallback(callback: (CommandSender, PaperMessage) -> Unit): PaperMessage {
        return PaginatedPaperMessage(lines, pagination, view, hooks + callback, visibilityResolver)
    }

    override fun send(receivers: Iterable<CommandSender>) {
        send(receivers, page = 0)
    }

    override fun send(receivers: Iterable<CommandSender>, page: Int) {
        if (view !is MessageView.Chat) {
            super.send(receivers, page)
            return
        }

        sendPaginated(receivers, page)
    }

    override fun recreate(
        lines: Iterable<Component>,
        hooks: List<(CommandSender, PaperMessage) -> Unit>
    ): PaperMessage {
        return PaperMessage.of(lines, pagination = pagination, view = view, callbacks = hooks, visibilityResolver = visibilityResolver)
    }

    private fun sendPaginated(receivers: Iterable<CommandSender>, page: Int = 0) {
        val pageLines = lines.toList()
        if (pageLines.isEmpty()) {
            return
        }

        val totalPages = max(ceil(pageLines.size.toDouble() / pagination.pageSize.toDouble()).toInt(), 1)
        val startIndex = page * pagination.pageSize
        val endIndex = startIndex + pagination.pageSize

        if (startIndex >= pageLines.size) {
            return
        }

        for (sender in receivers) {
            hooks.forEach { it(sender, this) }
            val context = PaginatedMessageContext(sender, page, totalPages)
            val components = ArrayList<Component>()

            if (pagination.header != null) {
                components.add(
                    pagination.header
                        .updatePaginationPlaceholders(page, totalPages, sender)
                        .resolveVisibilityConditions(context, visibilityResolver)
                        ?: Component.empty()
                )
            }
            for (i in startIndex until endIndex) {
                if (i >= pageLines.size) {
                    break
                }
                pageLines[i].component
                    .updatePaginationPlaceholders(page, totalPages, sender)
                    .resolveVisibilityConditions(context, visibilityResolver)
                    ?.resolvePageCallbacks(page, totalPages, sender, this)
                    ?.let(components::add)
            }
            if (pagination.footer != null) {
                pagination.footer
                    .updatePaginationPlaceholders(page, totalPages, sender)
                    .resolveVisibilityConditions(context, visibilityResolver)
                    ?.resolvePageCallbacks(page, totalPages, sender, this)
                    ?.let(components::add)
            }

            val component = Component.join(JoinConfiguration.newlines(), components)
            sender.sendMessage(component)
        }
    }

    private fun Component.updatePaginationPlaceholders(page: Int, totalPages: Int, sender: CommandSender) = replacePlaceholders(
        mapOf(
            "aq-player" to if (sender is Player) sender.name else "*console",
            "aq-page" to page.toString(),
            "aq-prev-page" to max(page - 1, 0).toString(),
            "aq-next-page" to min(totalPages - 1, page + 1).toString()
        )
    )
}
