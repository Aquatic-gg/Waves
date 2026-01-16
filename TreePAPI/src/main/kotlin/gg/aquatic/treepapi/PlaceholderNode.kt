package gg.aquatic.treepapi

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer

class PlaceholderNode(
    val id: String? = null
) {
    var handler: (PlaceholderArgumentContext.() -> String)? = null
    private val literalChildren = HashMap<String, PlaceholderNode>()
    private val argumentChildren = mutableListOf<PlaceholderNode>()
    var argumentParser: ((String) -> Any?)? = null

    operator fun String.invoke(block: PlaceholderNode.() -> Unit) {
        val node = PlaceholderNode(this.lowercase())
        node.block()
        literalChildren[this.lowercase()] = node
    }

    fun argument(id: String, parser: (String) -> Any?, block: PlaceholderNode.() -> Unit) {
        val node = PlaceholderNode(id)
        node.argumentParser = parser
        node.block()
        argumentChildren.add(node)
    }

    fun playerArgument(id: String, block: PlaceholderNode.() -> Unit) =
        argument(id, { Bukkit.getPlayer(it) }, block)

    fun offlinePlayerArgument(id: String, block: PlaceholderNode.() -> Unit) =
        argument(id, { Bukkit.getOfflinePlayer(it) }, block)

    fun stringArgument(id: String, block: PlaceholderNode.() -> Unit) =
        argument(id, { it }, block)

    fun intArgument(id: String, block: PlaceholderNode.() -> Unit) =
        argument(id, { it.toIntOrNull() }, block)

    fun handle(block: PlaceholderArgumentContext.() -> String) {
        this.handler = block
    }

    inline fun <reified R : Any> handleTyped(crossinline block: PlaceholderArgumentContext.() -> String) {
        this.handler = {
            if (binder is R) block(this) else ""
        }
    }

    fun resolve(binder: OfflinePlayer, tokens: List<String>, currentArgs: Map<String, Any?>, index: Int = 0): String? {
        if (index >= tokens.size) {
            return handler?.invoke(PlaceholderArgumentContext(binder, currentArgs))
        }

        val currentToken = tokens[index]
        val lowerToken = currentToken.lowercase()

        // 1. Try Literals (O(1) lookup)
        literalChildren[lowerToken]?.let { child ->
            val result = child.resolve(binder, tokens, currentArgs, index + 1)
            if (result != null) return result
        }

        // 2. Try Arguments
        for (child in argumentChildren) {
            val parsed = child.argumentParser?.invoke(currentToken)
            if (parsed != null) {
                val newArgs = if (currentArgs.isEmpty()) HashMap() else HashMap(currentArgs)
                newArgs[child.id!!] = parsed
                val result = child.resolve(binder, tokens, newArgs, index + 1)
                if (result != null) return result
            }
        }

        // Fallback to current node's handler (allows for optional trailing arguments)
        return handler?.invoke(PlaceholderArgumentContext(binder, currentArgs))
    }
}