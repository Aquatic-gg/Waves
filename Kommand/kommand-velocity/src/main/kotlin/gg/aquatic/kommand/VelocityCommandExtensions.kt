package gg.aquatic.kommand

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.velocitypowered.api.command.BrigadierCommand
import com.velocitypowered.api.command.CommandSource
import com.velocitypowered.api.proxy.Player
import com.velocitypowered.api.proxy.ProxyServer

fun command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit
) {
    val proxyServer = VelocityKommandConfig.proxyServer
        ?: error("VelocityKommandConfig.proxyServer must be configured before registering commands.")

    proxyServer.command(name, *aliases, block = block)
}

fun ProxyServer.command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit
) {
    val builder = LiteralArgumentBuilder.literal<CommandSource>(name)
    CommandBuilder(builder = builder, senderResolver = { source -> source }).apply(block)

    val metaBuilder = commandManager.metaBuilder(name)
    if (aliases.isNotEmpty()) {
        metaBuilder.aliases(*aliases)
    }

    commandManager.register(metaBuilder.build(), BrigadierCommand(builder.build()))
}

fun CommandBuilder<CommandSource, CommandSource>.hasPermission(permission: String) {
    requires { sender.hasPermission(permission) }
}

fun ExecutionContext<CommandSource, CommandSource>.player(id: String): Player? {
    val direct = getOrNull<Player>(id)
    if (direct != null) return direct

    val name = getOrNull<String>(id) ?: return null
    return VelocityKommandConfig.proxyServer?.getPlayer(name)?.orElse(null)
}

fun CommandBuilder<CommandSource, CommandSource>.playerArgument(
    id: String,
    includeSelf: Boolean,
    onInvalid: (ExecutionContext<CommandSource, CommandSource>.() -> Unit)? = null,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit = {}
) {
    playerArgumentInternal(
        id = id,
        onInvalid = onInvalid,
        finalFilter = { ctx, player ->
            if (!includeSelf) (ctx.source as? Player)?.uniqueId != player.uniqueId else true
        },
        block = block
    )
}

fun CommandBuilder<CommandSource, CommandSource>.playerArgument(
    id: String,
    filter: (CommandContext<CommandSource>, Player) -> Boolean,
    onInvalid: (ExecutionContext<CommandSource, CommandSource>.() -> Unit)? = null,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit = {}
) {
    playerArgumentInternal(id, filter, onInvalid, block)
}

fun CommandBuilder<CommandSource, CommandSource>.playerArgument(
    id: String,
    onInvalid: (ExecutionContext<CommandSource, CommandSource>.() -> Unit)? = null,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit = {}
) {
    playerArgumentInternal(id, { _, _ -> true }, onInvalid, block)
}

private fun CommandBuilder<CommandSource, CommandSource>.playerArgumentInternal(
    id: String,
    finalFilter: (CommandContext<CommandSource>, Player) -> Boolean,
    onInvalid: (ExecutionContext<CommandSource, CommandSource>.() -> Unit)?,
    block: CommandBuilder<CommandSource, CommandSource>.() -> Unit
) {
    mappedStringArgument(
        id = id,
        onInvalid = onInvalid,
        parser = { ctx, raw ->
            VelocityKommandConfig.proxyServer
                ?.getPlayer(raw)
                ?.orElse(null)
                ?.takeIf { player -> finalFilter(ctx, player) }
        }
    ) {
        suggests { ctx, builder ->
            val proxyServer = VelocityKommandConfig.proxyServer
                ?: return@suggests builder.buildFuture()

            val remaining = builder.remaining.lowercase()
            proxyServer.allPlayers.forEach { player ->
                if (finalFilter(ctx, player) && player.username.lowercase().startsWith(remaining)) {
                    builder.suggest(player.username)
                }
            }
            builder.buildFuture()
        }
        block()
    }
}
