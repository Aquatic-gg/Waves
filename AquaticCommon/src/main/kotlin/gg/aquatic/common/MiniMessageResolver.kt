package gg.aquatic.common

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

fun interface MiniMessageResolver {

    fun parse(string: String): Component

    companion object {
        fun of(miniMessage: MiniMessage): MiniMessageResolver = { miniMessage.deserialize(it) }
    }
}