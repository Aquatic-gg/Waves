package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.message.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender

open class PaperMessage protected constructor(
    val lines: Iterable<MessageLine>,
    val view: MessageView = MessageView.Chat,
    protected val hooks: List<(CommandSender, PaperMessage) -> Unit> = emptyList(),
    internal val visibilityResolver: (suspend (MessageContext, List<ComponentVisibilityPayload>) -> Boolean)? = null,
) : Message<PaperMessage> {

    companion object {
        fun of(
            vararg lines: Component,
            pagination: PaginationSettings? = null,
            view: MessageView = MessageView.Chat,
            callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList(),
            visibilityResolver: (suspend (MessageContext, List<ComponentVisibilityPayload>) -> Boolean)? = null,
        ) = of(lines.asIterable(), pagination, view, callbacks, visibilityResolver)

        fun of(
            lines: Iterable<Component>,
            pagination: PaginationSettings? = null,
            view: MessageView = MessageView.Chat,
            callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList(),
            visibilityResolver: (suspend (MessageContext, List<ComponentVisibilityPayload>) -> Boolean)? = null,
        ): PaperMessage {
            val mapped = lines.map { MessageLine(it, it.findPlaceholders()) }
            return if (pagination != null) {
                PaginatedPaperMessage(mapped, pagination, view, callbacks, visibilityResolver)
            } else {
                PaperMessage(mapped, view, callbacks, visibilityResolver)
            }
        }
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

    open fun withCallback(callback: (CommandSender, PaperMessage) -> Unit): PaperMessage =
        recreate(lines.map { it.component }, hooks + callback)

    fun send(vararg receiver: CommandSender) = send(receiver.asIterable())

    open fun send(receivers: Iterable<CommandSender>) {
        send(receivers, page = 0)
    }

    open fun send(receivers: Iterable<CommandSender>, page: Int) {
        val componentToSend = if (view is MessageView.Chat) cachedComponent ?: Component.join(
            JoinConfiguration.newlines(),
            lines.map { it.component }
        ) else null

        for (player in receivers) {
            hooks.forEach { it(player, this) }
            val context = SimpleMessageContext(player)
            val resolvedLines = lines.mapNotNull {
                it.component
                    .resolveVisibilityConditions(context, visibilityResolver)
                    ?.resolvePageCallbacks(0, 1, player, this)
            }
            when (view) {
                is MessageView.Chat -> {
                    val resolved = if (cachedComponent != null && isStatic) {
                        cachedComponent
                            .resolveVisibilityConditions(context, visibilityResolver)
                            ?.resolvePageCallbacks(0, 1, player, this)
                            ?: Component.empty()
                    } else {
                        Component.join(JoinConfiguration.newlines(), resolvedLines)
                    }
                    view.send(player, listOf(resolved))
                }
                is MessageView.ActionBar -> view.send(player, resolvedLines)
                is MessageView.Title -> view.send(player, resolvedLines)
            }
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
        return recreate(lines.map { line ->
            val filtered = replacements.filter { it.key in line.placeholders }
            if (filtered.isEmpty()) return@map line.component
            line.component.replacePlaceholders(filtered)
        })
    }

    override fun replace(updater: (String) -> String): PaperMessage {
        if (isStatic) return this
        return recreate(lines.map {
            if (it.placeholders.isEmpty()) return@map it.component
            it.component.replacePlaceholders(updater)
        })
    }

    fun replace(placeholder: String, component: Component): PaperMessage {
        if (isStatic) return this
        return recreate(lines.map { line ->
            if (!line.placeholders.contains(placeholder)) return@map line.component
            line.component.replaceWith(mapOf(placeholder to { component }))
        })
    }

    protected open fun recreate(
        lines: Iterable<Component>,
        hooks: List<(CommandSender, PaperMessage) -> Unit> = this.hooks
    ): PaperMessage {
        return PaperMessage.of(lines, view = view, callbacks = hooks, visibilityResolver = visibilityResolver)
    }
}
