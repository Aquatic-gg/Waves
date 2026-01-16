package gg.aquatic.replace

import org.bukkit.Bukkit
import kotlin.text.iterator

class PlaceholderDSLContext<T>(
    val binder: T,
    val arguments: Map<String, Any?>
) {
    inline fun <reified R> arg(id: String): R? = arguments[id] as? R
    fun string(id: String): String? = arg<String>(id)
}

class PlaceholderDSLNode<T>(
    val id: String? = null
) {
    var handler: (PlaceholderDSLContext<T>.() -> String)? = null
    private val literalChildren = HashMap<String, PlaceholderDSLNode<T>>()
    private val argumentChildren = mutableListOf<PlaceholderDSLNode<T>>()
    var argumentParser: ((String) -> Any?)? = null

    operator fun String.invoke(block: PlaceholderDSLNode<T>.() -> Unit) {
        val node = PlaceholderDSLNode<T>(this.lowercase())
        node.block()
        literalChildren[this.lowercase()] = node
    }

    fun argument(id: String, parser: (String) -> Any?, block: PlaceholderDSLNode<T>.() -> Unit) {
        val node = PlaceholderDSLNode<T>(id)
        node.argumentParser = parser
        node.block()
        argumentChildren.add(node)
    }

    fun stringArgument(id: String, block: PlaceholderDSLNode<T>.() -> Unit) =
        argument(id, { it }, block)

    fun intArgument(id: String, block: PlaceholderDSLNode<T>.() -> Unit) =
        argument(id, { it.toIntOrNull() }, block)

    fun playerArgument(id: String, block: PlaceholderDSLNode<T>.() -> Unit) =
        argument(id, { Bukkit.getPlayer(it) }, block)

    fun handle(block: PlaceholderDSLContext<T>.() -> String) {
        this.handler = block
    }

    fun resolve(binder: T, tokens: List<String>, currentArgs: Map<String, Any?>, index: Int = 0): String? {
        if (index >= tokens.size) {
            return handler?.invoke(PlaceholderDSLContext(binder, currentArgs))
        }

        val currentToken = tokens[index]
        val lowerToken = currentToken.lowercase()

        literalChildren[lowerToken]?.let { child ->
            val result = child.resolve(binder, tokens, currentArgs, index + 1)
            if (result != null) return result
        }

        for (child in argumentChildren) {
            val parsed = child.argumentParser?.invoke(currentToken)
            if (parsed != null) {
                val newArgs = if (currentArgs.isEmpty()) HashMap() else HashMap(currentArgs)
                newArgs[child.id!!] = parsed
                val result = child.resolve(binder, tokens, newArgs, index + 1)
                if (result != null) return result

                val fallback = handler?.invoke(PlaceholderDSLContext(binder, newArgs))
                if (fallback != null) return fallback
            }
        }

        return handler?.invoke(PlaceholderDSLContext(binder, currentArgs))
    }
}

inline fun <reified T> dslPlaceholder(
    identifier: String,
    isConst: Boolean = false,
    block: PlaceholderDSLNode<T>.() -> Unit
): Placeholder<T> {
    val root = PlaceholderDSLNode<T>()
    root.block()

    val value = Placeholder.Literal<T>(identifier, isConst) { binder, text ->
        val params = text.removePrefix("${identifier}_")
        val tokens = if (params == text || params.isEmpty()) {
            if (text == identifier) emptyList() else parsePapiStyle(text)
        } else {
            parsePapiStyle(params)
        }
        root.resolve(binder, tokens, emptyMap()) ?: ""
    }

    Placeholders.register(T::class.java, value)

    return value
}

fun parsePapiStyle(input: String): List<String> {
    if (input.isEmpty()) return emptyList()
    val result = mutableListOf<String>()
    val builder = StringBuilder()
    var inQuotes = false

    for (char in input) {
        when (char) {
            '"' -> inQuotes = !inQuotes
            '_' -> {
                if (!inQuotes) {
                    if (builder.isNotEmpty()) {
                        result.add(builder.toString())
                        builder.setLength(0)
                    }
                } else {
                    builder.append(char)
                }
            }
            else -> builder.append(char)
        }
    }
    if (builder.isNotEmpty()) result.add(builder.toString())
    return result
}
