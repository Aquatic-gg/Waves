package gg.aquatic.kommand

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

fun command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit
) {
    val commands = PaperKommandConfig.commands
    if (commands != null) {
        commands.command(name, *aliases, block = block)
        return
    }

    PaperKommandConfig.commandDispatcher.command(name, *aliases, block = block)
}

fun Commands.command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit
) {
    val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
    CommandBuilder(builder = builder, senderResolver = { source -> source.sender }).apply(block)
    register(builder.build(), aliases.toList())
}

fun CommandDispatcher<CommandSourceStack>.command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit
) {
    command(name, *aliases, senderResolver = { source -> source.sender }, block = block)
}

fun CommandBuilder<CommandSourceStack, CommandSender>.hasPermission(permission: String) {
    requires { sender.hasPermission(permission) }
}

fun ExecutionContext<CommandSourceStack, CommandSender>.player(id: String): Player? {
    val direct = getOrNull<Player>(id)
    if (direct != null) return direct

    val name = getOrNull<String>(id) ?: return null
    return Bukkit.getPlayerExact(name)
}

fun CommandBuilder<CommandSourceStack, CommandSender>.playerArgument(
    id: String,
    includeSelf: Boolean,
    onInvalid: (ExecutionContext<CommandSourceStack, CommandSender>.() -> Unit)? = null,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit = {}
) {
    playerArgumentInternal(
        id = id,
        onInvalid = onInvalid,
        finalFilter = { ctx, player ->
            if (!includeSelf) (ctx.source.sender as? Player)?.uniqueId != player.uniqueId else true
        },
        block = block
    )
}

fun CommandBuilder<CommandSourceStack, CommandSender>.playerArgument(
    id: String,
    filter: (CommandContext<CommandSourceStack>, Player) -> Boolean,
    onInvalid: (ExecutionContext<CommandSourceStack, CommandSender>.() -> Unit)? = null,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit = {}
) {
    playerArgumentInternal(id, filter, onInvalid, block)
}

fun CommandBuilder<CommandSourceStack, CommandSender>.playerArgument(
    id: String,
    onInvalid: (ExecutionContext<CommandSourceStack, CommandSender>.() -> Unit)? = null,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit = {}
) {
    playerArgumentInternal(id, { _, _ -> true }, onInvalid, block)
}

private fun CommandBuilder<CommandSourceStack, CommandSender>.playerArgumentInternal(
    id: String,
    finalFilter: (CommandContext<CommandSourceStack>, Player) -> Boolean,
    onInvalid: (ExecutionContext<CommandSourceStack, CommandSender>.() -> Unit)?,
    block: CommandBuilder<CommandSourceStack, CommandSender>.() -> Unit
) {
    mappedStringArgument(
        id = id,
        onInvalid = onInvalid,
        parser = { ctx, raw ->
            Bukkit.getPlayerExact(raw)?.takeIf { player -> finalFilter(ctx, player) }
        }
    ) {
        suggests { ctx, builder ->
            val remaining = builder.remaining.lowercase()
            Bukkit.getOnlinePlayers().forEach { player ->
                if (finalFilter(ctx, player) && player.name.lowercase().startsWith(remaining)) {
                    builder.suggest(player.name)
                }
            }
            builder.buildFuture()
        }
        block()
    }
}
