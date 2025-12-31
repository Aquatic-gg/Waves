package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.message.Message
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.JoinConfiguration
import org.bukkit.command.CommandSender

class PaperMessage private constructor(
    val lines: Iterable<MessageLine>,
    private val hooks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()
) : Message<PaperMessage> {

    companion object {
        fun of(vararg lines: Component, callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()) = PaperMessage(lines.map { MessageLine(it, it.findPlaceholders()) }, callbacks)
        fun of(lines: Iterable<Component>, callbacks: List<(CommandSender, PaperMessage) -> Unit> = emptyList()) = PaperMessage(lines.map { MessageLine(it, it.findPlaceholders()) }, callbacks)
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
        }, hooks)
    }

    override fun replace(updater: (String) -> String): PaperMessage {
        if (isStatic) return this
        return of(lines.map {
            if (it.placeholders.isEmpty()) return@map it.component
            it.component.replacePlaceholders(updater)
        }, hooks)
    }

    fun replace(placeholder: String, component: Component): PaperMessage {
        if (isStatic) return this
        return of(lines.map {
            if (!it.placeholders.contains(placeholder)) return@map it.component
            it.component.replaceWith(mapOf(placeholder to { component }))
        }, hooks)
    }
}