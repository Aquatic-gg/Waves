package gg.aquatic.kommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.arguments.BoolArgumentType
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.LongArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import io.papermc.paper.command.brigadier.Commands
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import kotlinx.coroutines.launch
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.math.BigDecimal
import java.math.BigInteger
import java.util.concurrent.CompletableFuture
import java.util.regex.Pattern

@DslMarker
annotation class BrigadierDsl

enum class StringArgumentFormat {
    WORD,
    STRING,
    GREEDY_STRING,
}

@BrigadierDsl
class CommandBuilder<S : CommandSourceStack>(
    val builder: ArgumentBuilder<S, *>,
    val inheritedRunnables: MutableList<ExecutionContext<S, *>.() -> Boolean> = mutableListOf(),
    private val argumentMappers: MutableMap<String, (CommandContext<S>) -> Any?> = mutableMapOf(),
    private val invalidArgumentHandlers: MutableMap<String, ExecutionContext<S, CommandSender>.() -> Unit> = mutableMapOf(),
    private var inheritedRunnableCount: Int = inheritedRunnables.size,
    private val childBuilders: MutableList<CommandBuilder<S>> = mutableListOf(),
    private val reattachToParent: (() -> Unit)? = null
) {

    operator fun String.invoke(block: CommandBuilder<S>.() -> Unit) {
        val literal = LiteralArgumentBuilder.literal<S>(this)
        attachChildBuilder(literal, block)
    }

    fun <T> argument(name: String, type: ArgumentType<T>, block: CommandBuilder<S>.() -> Unit = {}) {
        val arg = RequiredArgumentBuilder.argument<S, T>(name, type)
        attachChildBuilder(arg, block)
    }

    fun onInvalidArgument(id: String, handler: ExecutionContext<S, CommandSender>.() -> Unit) {
        invalidArgumentHandlers[id] = handler
    }

    fun playerArgument(
        id: String,
        includeSelf: Boolean,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid != null) {
            invalidArgumentHandlers[id] = onInvalid
        }
        playerArgumentInternal(id, { ctx, player ->
            if (!includeSelf) (ctx.source.sender as? Player)?.uniqueId != player.uniqueId else true
        }, block)
    }

    fun playerArgument(
        id: String,
        filter: (CommandContext<S>, Player) -> Boolean,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid != null) {
            invalidArgumentHandlers[id] = onInvalid
        }
        playerArgumentInternal(id, filter, block)
    }

    fun playerArgument(
        id: String,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid != null) {
            invalidArgumentHandlers[id] = onInvalid
        }
        playerArgumentInternal(id, { _, _ -> true }, block)
    }

    private fun playerArgumentInternal(
        id: String,
        finalFilter: (CommandContext<S>, Player) -> Boolean,
        block: CommandBuilder<S>.() -> Unit
    ) {
        argumentMappers[id] = { ctx ->
            val name = try {
                StringArgumentType.getString(ctx, id)
            } catch (_: Exception) {
                null
            }
            name?.let(Bukkit::getPlayerExact)?.takeIf { finalFilter(ctx, it) }
        }

        argument(id, StringArgumentType.word()) {
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

    fun requires(condition: (S) -> Boolean) {
        builder.requires(condition)
    }

    fun hasPermission(permission: String) {
        requires { it.sender.hasPermission(permission) }
    }

    fun suggests(block: (context: CommandContext<S>, builder: SuggestionsBuilder) -> CompletableFuture<Suggestions>) {
        if (builder is RequiredArgumentBuilder<S, *>) {
            builder.suggests(block)
        }
    }

    fun suggestsAsync(
        scope: CoroutineScope = KommandConfig.commandScope,
        block: suspend (context: CommandContext<S>, builder: SuggestionsBuilder) -> Suggestions
    ) {
        suggests { context, builder ->
            scope.async {
                block(context, builder)
            }.asCompletableFuture()
        }
    }

    fun stringArgument(id: String, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, StringArgumentType.word(), block)
    }

    fun greedyStringArgument(id: String, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, StringArgumentType.greedyString(), block)
    }

    private fun <T : Any> mappedWordArgument(
        id: String,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)?,
        parser: (String) -> T?,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid != null) {
            invalidArgumentHandlers[id] = onInvalid
        }
        argumentMappers[id] = { ctx ->
            val raw = try {
                StringArgumentType.getString(ctx, id)
            } catch (_: Exception) {
                null
            }
            raw?.let(parser)
        }
        argument(id, StringArgumentType.word(), block)
    }

    fun byteArgument(id: String, min: Byte = Byte.MIN_VALUE, max: Byte = Byte.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        intArgument(id, min.toInt(), max.toInt(), block)
    }

    fun byteArgument(
        id: String,
        min: Byte = Byte.MIN_VALUE,
        max: Byte = Byte.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toByteOrNull()?.takeIf { it in min..max } }, block)
    }

    fun shortArgument(id: String, min: Short = Short.MIN_VALUE, max: Short = Short.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        intArgument(id, min.toInt(), max.toInt(), block)
    }

    fun shortArgument(
        id: String,
        min: Short = Short.MIN_VALUE,
        max: Short = Short.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toShortOrNull()?.takeIf { it in min..max } }, block)
    }

    fun intArgument(id: String, min: Int = Int.MIN_VALUE, max: Int = Int.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, IntegerArgumentType.integer(min, max), block)
    }

    fun intArgument(
        id: String,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid == null) {
            intArgument(id, min, max, block)
            return
        }

        mappedWordArgument(id, onInvalid, { raw -> raw.toIntOrNull()?.takeIf { it in min..max } }, block)
    }

    fun longArgument(id: String, min: Long = Long.MIN_VALUE, max: Long = Long.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, LongArgumentType.longArg(min, max), block)
    }

    fun longArgument(
        id: String,
        min: Long = Long.MIN_VALUE,
        max: Long = Long.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid == null) {
            longArgument(id, min, max, block)
            return
        }

        mappedWordArgument(id, onInvalid, { raw -> raw.toLongOrNull()?.takeIf { it in min..max } }, block)
    }

    fun floatArgument(id: String, min: Float = -Float.MAX_VALUE, max: Float = Float.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, FloatArgumentType.floatArg(min, max), block)
    }

    fun floatArgument(
        id: String,
        min: Float = -Float.MAX_VALUE,
        max: Float = Float.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid == null) {
            floatArgument(id, min, max, block)
            return
        }

        mappedWordArgument(id, onInvalid, { raw -> raw.toFloatOrNull()?.takeIf { it in min..max } }, block)
    }

    fun doubleArgument(id: String, min: Double = -Double.MAX_VALUE, max: Double = Double.MAX_VALUE, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, DoubleArgumentType.doubleArg(min, max), block)
    }

    fun doubleArgument(
        id: String,
        min: Double = -Double.MAX_VALUE,
        max: Double = Double.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        if (onInvalid == null) {
            doubleArgument(id, min, max, block)
            return
        }

        mappedWordArgument(id, onInvalid, { raw -> raw.toDoubleOrNull()?.takeIf { it in min..max } }, block)
    }

    fun uByteArgument(
        id: String,
        min: UByte = UByte.MIN_VALUE,
        max: UByte = UByte.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toUByteOrNull()?.takeIf { it in min..max } }, block)
    }

    fun uShortArgument(
        id: String,
        min: UShort = UShort.MIN_VALUE,
        max: UShort = UShort.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toUShortOrNull()?.takeIf { it in min..max } }, block)
    }

    fun uIntArgument(
        id: String,
        min: UInt = UInt.MIN_VALUE,
        max: UInt = UInt.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toUIntOrNull()?.takeIf { it in min..max } }, block)
    }

    fun uLongArgument(
        id: String,
        min: ULong = ULong.MIN_VALUE,
        max: ULong = ULong.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw -> raw.toULongOrNull()?.takeIf { it in min..max } }, block)
    }

    fun bigIntegerArgument(
        id: String,
        min: BigInteger? = null,
        max: BigInteger? = null,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toBigIntegerOrNull()?.takeIf { value ->
                (min == null || value >= min) && (max == null || value <= max)
            }
        }, block)
    }

    fun bigDecimalArgument(
        id: String,
        min: BigDecimal? = null,
        max: BigDecimal? = null,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toBigDecimalOrNull()?.takeIf { value ->
                (min == null || value >= min) && (max == null || value <= max)
            }
        }, block)
    }

    fun booleanArgument(id: String, block: CommandBuilder<S>.() -> Unit = {}) {
        argument(id, BoolArgumentType.bool(), block)
    }

    fun namedArguments(
        id: String,
        options: Map<String, ArgumentType<*>>,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        argumentMappers[id] = { ctx ->
            val input = try {
                StringArgumentType.getString(ctx, id)
            } catch (_: Exception) {
                ""
            }
            val found = mutableMapOf<String, Any>()

            val matcher = Pattern.compile("-(\\w+):(\\S+)").matcher(input)
            while (matcher.find()) {
                val key = matcher.group(1)
                val valueStr = matcher.group(2)
                val type = options[key] ?: continue

                try {
                    val reader = StringReader(valueStr)
                    found[key] = type.parse(reader)!!
                } catch (_: Exception) {
                }
            }
            found
        }

        greedyStringArgument(id) {
            suggests { _, builder ->
                val remaining = builder.remaining.lowercase()
                options.keys.forEach { key ->
                    val flag = "-$key:"
                    if (flag.startsWith(remaining) || remaining.contains(" ")) {
                        builder.suggest(flag)
                    }
                }
                builder.buildFuture()
            }
            block()
        }
    }

    fun flagsArgument(
        id: String,
        allowedFlags: List<String>,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        argumentMappers[id] = { ctx ->
            val input = try {
                StringArgumentType.getString(ctx, id)
            } catch (_: Exception) {
                ""
            }
            val found = mutableSetOf<String>()

            val matcher = Pattern.compile("(--?\\w+)").matcher(input)
            while (matcher.find()) {
                val flag = matcher.group(1)
                if (flag in allowedFlags) {
                    found.add(flag)
                }
            }
            found
        }

        greedyStringArgument(id) {
            suggests { _, builder ->
                val input = builder.remaining.lowercase()
                val currentFlags = input.split(" ")

                allowedFlags.forEach { flag ->
                    if (flag.lowercase() !in currentFlags && flag.lowercase().startsWith(currentFlags.last())) {
                        builder.suggest(flag)
                    }
                }
                builder.buildFuture()
            }
            block()
        }
    }

    fun listArgument(
        id: String,
        values: (CommandContext<S>) -> Iterable<String>,
        format: StringArgumentFormat = StringArgumentFormat.WORD,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        listArgument(id, values, { it }, format, block)
    }

    fun <T : Any> listArgument(
        id: String,
        values: (CommandContext<S>) -> Iterable<T>,
        mapper: (T) -> String,
        format: StringArgumentFormat = StringArgumentFormat.WORD,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        argumentMappers[id] = { ctx ->
            val input = try {
                ctx.getArgument(id, String::class.java)
            } catch (_: Exception) {
                null
            }
            if (input == null) null else values(ctx).find { mapper(it) == input }
        }

        val argumentType = when (format) {
            StringArgumentFormat.WORD -> StringArgumentType.word()
            StringArgumentFormat.STRING -> StringArgumentType.string()
            StringArgumentFormat.GREEDY_STRING -> StringArgumentType.greedyString()
        }

        val arg = RequiredArgumentBuilder.argument<S, String>(id, argumentType)
        arg.suggests { ctx, builder ->
            val remaining = builder.remaining.lowercase()
            values(ctx).forEach { item ->
                val str = mapper(item)
                if (str.lowercase().startsWith(remaining)) {
                    builder.suggest(quoteIfNeeded(str, format))
                }
            }
            builder.buildFuture()
        }

        attachChildBuilder(arg, block)
    }

    inline fun <reified T : CommandSender> execute(crossinline block: ExecutionContext<S, T>.() -> Boolean) {
        val wrappedBlock: ExecutionContext<S, *>.() -> Boolean = {
            @Suppress("UNCHECKED_CAST")
            if (sender is T) (this as ExecutionContext<S, T>).block() else false
        }

        inheritedRunnables.add(wrappedBlock)
        propagateRunnableToChildren(wrappedBlock)
        rebindExecution()
    }

    inline fun <reified T : CommandSender> suspendExecute(
        crossinline block: suspend ExecutionContext<S, T>.() -> Unit
    ) {
        execute<T> {
            KommandConfig.commandScope.launch {
                block()
            }
            true
        }
    }

    fun rebindExecution() {
        val runnablesSnapshot = inheritedRunnables.toList()
        val mappersSnapshot = argumentMappers.toMap()
        val invalidHandlersSnapshot = invalidArgumentHandlers.toMap()

        builder.executes { context ->
            val execContext = ExecutionContext<S, CommandSender>(
                context.source.sender,
                context,
                mappersSnapshot,
                invalidHandlersSnapshot
            )
            try {
                for (runnable in runnablesSnapshot) {
                    if (execContext.runnable()) break
                }
            } catch (_: HandledCommandException) {
            }
            Command.SINGLE_SUCCESS
        }
        reattachToParent?.invoke()
    }

    fun listArgument(
        id: String,
        values: List<String>,
        format: StringArgumentFormat = StringArgumentFormat.WORD,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        listArgument(id, { values }, { it }, format, block)
    }

    private fun attachChildBuilder(
        childBuilder: ArgumentBuilder<S, *>,
        block: CommandBuilder<S>.() -> Unit
    ) {
        val subBuilder = CommandBuilder(
            builder = childBuilder,
            inheritedRunnables = inheritedRunnables.toMutableList(),
            argumentMappers = argumentMappers.toMutableMap(),
            invalidArgumentHandlers = invalidArgumentHandlers.toMutableMap(),
            inheritedRunnableCount = inheritedRunnables.size,
            reattachToParent = {
                builder.then(childBuilder)
                reattachToParent?.invoke()
            }
        )
        childBuilders.add(subBuilder)
        subBuilder.block()
        builder.then(childBuilder)
        reattachToParent?.invoke()
    }

    @PublishedApi
    internal fun propagateRunnableToChildren(runnable: ExecutionContext<S, *>.() -> Boolean) {
        childBuilders.forEach { it.inheritRunnableFromAncestor(runnable) }
    }

    private fun inheritRunnableFromAncestor(runnable: ExecutionContext<S, *>.() -> Boolean) {
        inheritedRunnables.add(inheritedRunnableCount, runnable)
        inheritedRunnableCount++
        rebindExecution()
        childBuilders.forEach { it.inheritRunnableFromAncestor(runnable) }
    }
}

private fun quoteIfNeeded(value: String, format: StringArgumentFormat): String {
    if (format != StringArgumentFormat.STRING) return value

    val needsQuotes = value.any { !StringReader.isAllowedInUnquotedString(it) }
    if (!needsQuotes) return value

    val escaped = value.replace("\\", "\\\\").replace("\"", "\\\"")
    return "\"$escaped\""
}

class ExecutionContext<S : CommandSourceStack, out T : CommandSender>(
    val sender: T,
    val context: CommandContext<S>,
    private val mappers: Map<String, (CommandContext<S>) -> Any?> = emptyMap(),
    @PublishedApi
    internal val invalidHandlers: Map<String, ExecutionContext<S, CommandSender>.() -> Unit> = emptyMap()
) {
    inline fun <reified V> get(id: String): V {
        return getOrNull(id) ?: run {
            @Suppress("UNCHECKED_CAST")
            invalidHandlers[id]?.invoke(this as ExecutionContext<S, CommandSender>)
            if (id in invalidHandlers) {
                throw HandledCommandException
            }
            throw IllegalStateException("Required command argument '$id' is missing or failed to map.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    fun <V> getOrNull(id: String): V? {
        val mapper = mappers[id]
        if (mapper != null) {
            return mapper(context) as? V
        }

        return try {
            context.getArgument(id, Any::class.java) as? V
        } catch (_: Exception) {
            null
        }
    }

    fun player(id: String): Player? {
        val direct = getOrNull<Player>(id)
        if (direct != null) return direct

        val name = getOrNull<String>(id) ?: return null
        return Bukkit.getPlayerExact(name)
    }

    @Suppress("UNCHECKED_CAST")
    fun <V> named(id: String, key: String): V? {
        val map = getOrNull<Map<String, Any>>(id) ?: return null
        return map[key] as? V
    }

    fun <V> named(id: String, key: String, default: V): V {
        return named(id, key) ?: default
    }

    fun flags(id: String): Set<String> {
        return getOrNull<Set<String>>(id) ?: emptySet()
    }

    fun hasFlag(id: String, flag: String): Boolean {
        return flags(id).contains(flag)
    }

    fun int(id: String): Int = get(id)
    fun byte(id: String): Byte = get(id)
    fun short(id: String): Short = get(id)
    fun long(id: String): Long = get(id)
    fun float(id: String): Float = get(id)
    fun double(id: String): Double = get(id)
    fun uByte(id: String): UByte = get(id)
    fun uShort(id: String): UShort = get(id)
    fun uInt(id: String): UInt = get(id)
    fun uLong(id: String): ULong = get(id)
    fun bigInteger(id: String): BigInteger = get(id)
    fun bigDecimal(id: String): BigDecimal = get(id)
    fun boolean(id: String): Boolean = get(id)

    fun string(id: String): String = StringArgumentType.getString(context, id)
}

@PublishedApi
internal object HandledCommandException : CancellationException()

fun command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack>.() -> Unit
) {
    val commands = KommandConfig.commands
    if (commands != null) {
        val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
        CommandBuilder(builder).apply(block)
        commands.register(builder.build(), aliases.toList())
        return
    }

    val names = listOf(name) + aliases.toList()
    val dispatcher = KommandConfig.commandDispatcher
    names.forEach { cmdName ->
        val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(cmdName)
        CommandBuilder(builder).apply(block)
        dispatcher.register(builder)
    }
}

fun Commands.command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack>.() -> Unit
) {
    val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(name)
    CommandBuilder(builder).apply(block)
    this.register(builder.build(), aliases.toList())
}

fun CommandDispatcher<CommandSourceStack>.command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack>.() -> Unit
) {
    val names = listOf(name) + aliases.toList()
    names.forEach { cmdName ->
        val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(cmdName)
        CommandBuilder(builder).apply(block)
        this.register(builder)
    }
}

fun <S> CommandDispatcher<S>.register(builders: List<LiteralArgumentBuilder<S>>) {
    builders.forEach { this.register(it) }
}
