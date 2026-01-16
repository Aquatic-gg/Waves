package gg.aquatic.treepapi

import me.clip.placeholderapi.expansion.PlaceholderExpansion

fun placeholder(author: String, identifier: String, block: PlaceholderNode.() -> Unit): PlaceholderExpansion? {
    val root = PlaceholderNode()
    root.block()

    return PAPIUtil.registerExtension(author, identifier) { player, params ->
        val tokens = PAPIParser.parse(params)
        root.resolve(player, tokens, emptyMap()) ?: ""
    }
}