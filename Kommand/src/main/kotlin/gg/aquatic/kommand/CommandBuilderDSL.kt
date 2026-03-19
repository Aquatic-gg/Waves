package gg.aquatic.kommand

import com.mojang.brigadier.Command
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.*
import com.mojang.brigadier.builder.ArgumentBuilder
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.builder.RequiredArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import io.papermc.paper.command.brigadier.CommandSourceStack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.CancellationException
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

@BrigadierDsl
class CommandBuilder<S : CommandSourceStack>(
    val builder: ArgumentBuilder<S, *>,
    val inheritedRunnables: MutableList<ExecutionContext<S, *>.() -> Boolean> = mutableListOf(),
    private val argumentMappers: MutableMap<String, (CommandContext<S>) -> Any?> = mutableMapOf(),
    private val invalidArgumentHandlers: MutableMap<String, ExecutionContext<S, CommandSender>.() -> Unit> = mutableMapOf()
) {

    operator fun String.invoke(block: CommandBuilder<S>.() -> Unit) {
        val literal = LiteralArgumentBuilder.literal<S>(this)
        val subBuilder = CommandBuilder(
            literal,
            inheritedRunnables.toMutableList(),
            argumentMappers.toMutableMap(),
            invalidArgumentHandlers.toMutableMap()
        )
        subBuilder.block()
        builder.then(literal)
    }

    fun <T> argument(name: String, type: ArgumentType<T>, block: CommandBuilder<S>.() -> Unit = {}) {
        val arg = RequiredArgumentBuilder.argument<S, T>(name, type)
        val subBuilder = CommandBuilder(
            arg,
            inheritedRunnables.toMutableList(),
            argumentMappers.toMutableMap(),
            invalidArgumentHandlers.toMutableMap()
        )
        subBuilder.block()
        builder.then(arg)
    }

    fun onInvalidArgument(id: String, handler: ExecutionContext<S, CommandSender>.() -> Unit) {
        invalidArgumentHandlers[id] = handler
    }

    /**
     * Standard player argument with only an includeSelf toggle.
     */
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

    /**
     * Player argument with a custom filter.
     */
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

    /**
     * Simple player argument (includes everyone).
     */
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

    /**
     * Private helper to avoid code duplication
     */
    private fun playerArgumentInternal(
        id: String,
        finalFilter: (CommandContext<S>, Player) -> Boolean,
        block: CommandBuilder<S>.() -> Unit
    ) {
        argumentMappers[id] = { ctx ->
            val playerName = try {
                StringArgumentType.getString(ctx, id)
            } catch (_: Exception) {
                null
            }
            val resolved = playerName?.let(Bukkit::getPlayerExact)

            resolved?.takeIf { finalFilter(ctx, it) }
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
        scope: CoroutineScope = KommandConfig.commandScope, block: suspend (context: CommandContext<S>, builder: SuggestionsBuilder) -> Suggestions
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
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toByteOrNull()?.takeIf { it in min..max }
        }, block)
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
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toShortOrNull()?.takeIf { it in min..max }
        }, block)
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

        mappedWordArgument(id, onInvalid, { raw ->
            raw.toIntOrNull()?.takeIf { it in min..max }
        }, block)
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

        mappedWordArgument(id, onInvalid, { raw ->
            raw.toLongOrNull()?.takeIf { it in min..max }
        }, block)
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

        mappedWordArgument(id, onInvalid, { raw ->
            raw.toFloatOrNull()?.takeIf { it in min..max }
        }, block)
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

        mappedWordArgument(id, onInvalid, { raw ->
            raw.toDoubleOrNull()?.takeIf { it in min..max }
        }, block)
    }

    fun uByteArgument(
        id: String,
        min: UByte = UByte.MIN_VALUE,
        max: UByte = UByte.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toUByteOrNull()?.takeIf { it in min..max }
        }, block)
    }

    fun uShortArgument(
        id: String,
        min: UShort = UShort.MIN_VALUE,
        max: UShort = UShort.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toUShortOrNull()?.takeIf { it in min..max }
        }, block)
    }

    fun uIntArgument(
        id: String,
        min: UInt = UInt.MIN_VALUE,
        max: UInt = UInt.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toUIntOrNull()?.takeIf { it in min..max }
        }, block)
    }

    fun uLongArgument(
        id: String,
        min: ULong = ULong.MIN_VALUE,
        max: ULong = ULong.MAX_VALUE,
        onInvalid: (ExecutionContext<S, CommandSender>.() -> Unit)? = null,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        mappedWordArgument(id, onInvalid, { raw ->
            raw.toULongOrNull()?.takeIf { it in min..max }
        }, block)
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

    /**
     * Named Arguments (e.g., -amount:5 -radius:10.5)
     */
    fun namedArguments(
        id: String,
        options: Map<String, ArgumentType<*>>,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        argumentMappers[id] = { ctx ->
            val input = try { StringArgumentType.getString(ctx, id) } catch (e: Exception) { "" }
            val found = mutableMapOf<String, Any>()

            // Regex to find -key:value
            val matcher = Pattern.compile("-(\\w+):(\\S+)").matcher(input)
            while (matcher.find()) {
                val key = matcher.group(1)
                val valueStr = matcher.group(2)
                val type = options[key] ?: continue

                try {
                    // Use Brigadier's reader to parse the specific type
                    val reader = StringReader(valueStr)
                    found[key] = type.parse(reader)!!
                } catch (_: Exception) {}
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
        // Register the mapper to parse the greedy string into a Set
        argumentMappers[id] = { ctx ->
            val input = try { StringArgumentType.getString(ctx, id) } catch (e: Exception) { "" }
            val found = mutableSetOf<String>()

            // Regex to find words starting with - or --
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
            suggests { ctx, builder ->
                val input = builder.remaining.lowercase()
                val currentFlags = input.split(" ")

                // Suggest flags that haven't been typed yet
                allowedFlags.forEach { flag ->
                    if (flag.lowercase() !in currentFlags && flag.lowercase().startsWith(currentFlags.last())) {
                        // We suggest the flag by replacing only the last partial word
                        builder.suggest(flag)
                    }
                }
                builder.buildFuture()
            }
            block()
        }
    }

    fun <T : Any> listArgument(
        id: String,
        values: (CommandContext<S>) -> Iterable<T>,
        mapper: (T) -> String,
        block: CommandBuilder<S>.() -> Unit = {}
    ) {
        // Register the "parser" for this ID in the CURRENT builder
        argumentMappers[id] = { ctx ->
            val input = try {
                StringArgumentType.getString(ctx, id)
            } catch (e: Exception) {
                null
            }
            if (input == null) null
            else values(ctx).find { mapper(it) == input }
        }

        val arg = RequiredArgumentBuilder.argument<S, String>(id, StringArgumentType.word())
        arg.suggests { ctx, builder ->
            val remaining = builder.remaining.lowercase()
            values(ctx).forEach { item ->
                val str = mapper(item)
                if (str.lowercase().startsWith(remaining)) builder.suggest(str)
            }
            builder.buildFuture()
        }

        val subBuilder = CommandBuilder(
            arg,
            inheritedRunnables.toMutableList(),
            argumentMappers.toMutableMap(),
            invalidArgumentHandlers.toMutableMap()
        )
        subBuilder.block()
        builder.then(arg)
    }

    inline fun <reified T : CommandSender> execute(crossinline block: ExecutionContext<S, T>.() -> Boolean) {
        val wrappedBlock: ExecutionContext<S, *>.() -> Boolean = {
            @Suppress("UNCHECKED_CAST")
            if (sender is T) (this as ExecutionContext<S, T>).block() else false
        }

        inheritedRunnables.add(wrappedBlock)
        rebindExecution()
    }

    inline fun <reified T : CommandSender> suspendExecute(
        crossinline block: suspend ExecutionContext<S, T>.() -> Unit
    ) {
        execute<T> {
            KommandConfig.commandScope.launch {
                block()
            }
            true // We return true because the logic is now handled async
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
    }

    fun listArgument(id: String, values: List<String>, block: CommandBuilder<S>.() -> Unit = {}) {
        listArgument(id, { values }, { it }, block)
    }
}

class ExecutionContext<S : CommandSourceStack, out T : CommandSender>(
    val sender: T,
    val context: CommandContext<S>,
    private val mappers: Map<String, (CommandContext<S>) -> Any?> = emptyMap(),
    @PublishedApi
    internal val invalidHandlers: Map<String, ExecutionContext<S, CommandSender>.() -> Unit> = emptyMap()
) {
    /**
     * Retrieves an argument that is expected to exist.
     * Throws IllegalStateException if the argument is missing or mapping failed.
     */
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

    /**
     * Safely retrieves an argument or returns null if it doesn't exist.
     */
    @Suppress("UNCHECKED_CAST")
    fun <V> getOrNull(id: String): V? {
        // Check custom mappers (listArgument, flags, etc.)
        val mapper = mappers[id]
        if (mapper != null) {
            return mapper(context) as? V
        }

        // Fallback to raw Brigadier
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

    /**
     * Retrieves a named argument value (e.g., -amount:5)
     */
    @Suppress("UNCHECKED_CAST")
    fun <V> named(id: String, key: String): V? {
        val map = getOrNull<Map<String, Any>>(id) ?: return null
        return map[key] as? V
    }

    fun <V> named(id: String, key: String, default: V): V {
        return named(id, key) ?: default
    }

    /**
     * Retrieves the set of flags found in the command
     */
    fun flags(id: String): Set<String> {
        return getOrNull<Set<String>>(id) ?: emptySet()
    }

    /**
     * Shorthand to check if a specific flag was present
     */
    fun hasFlag(id: String, flag: String): Boolean {
        return flags(id).contains(flag)
    }

    fun int(id: String): Int = IntegerArgumentType.getInteger(context, id)
    fun byte(id: String): Byte = get(id)
    fun short(id: String): Short = get(id)
    fun long(id: String): Long = get(id)
    fun float(id: String): Float = get(id)
    fun double(id: String): Double = DoubleArgumentType.getDouble(context, id)
    fun uByte(id: String): UByte = get(id)
    fun uShort(id: String): UShort = get(id)
    fun uInt(id: String): UInt = get(id)
    fun uLong(id: String): ULong = get(id)
    fun bigInteger(id: String): BigInteger = get(id)
    fun bigDecimal(id: String): BigDecimal = get(id)
    fun boolean(id: String): Boolean = BoolArgumentType.getBool(context, id)

    fun string(id: String): String = StringArgumentType.getString(context, id)
}

@PublishedApi
internal object HandledCommandException : CancellationException()

fun command(
    name: String,
    vararg aliases: String,
    block: CommandBuilder<CommandSourceStack>.() -> Unit
) {
    val names = listOf(name) + aliases.toList()
    val dispatcher = KommandConfig.commandDispatcher

    names.forEach { cmdName ->
        val builder = LiteralArgumentBuilder.literal<CommandSourceStack>(cmdName)
        CommandBuilder(builder).apply(block)
        dispatcher.register(builder)
    }
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

fun <S> CommandDispatcher<S>.register(
    builders: List<LiteralArgumentBuilder<S>>
) {
    builders.forEach { this.register(it) }
}
