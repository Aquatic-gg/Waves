package gg.aquatic.execute.minimessage

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import java.util.concurrent.TimeUnit

fun String.toMMComponent(): Component {
    return MiniMessage.builder()
        .editTags { b ->
            b.tag("ccmd") { a, b ->
                ConsoleCommandMMResolver.resolve(a, b)
            }
        }.build().deserialize(
            this
                .replace("§", "&")
                .replace("&a", "<green>")
                .replace("&c", "<red>")
                .replace("&b", "<aqua>")
                .replace("&e", "<yellow>")
                .replace("&6", "<gold>")
                .replace("&d", "<light_purple>")
                .replace("&f", "<white>")
                .replace("&3", "<dark_aqua>")
                .replace("&9", "<blue>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&4", "<dark_red>")
                .replace("&1", "<dark_blue>")
                .replace("&4", "<dark_red>")
                .replace("&8", "<dark_gray>")
                .replace("&2", "<dark_green>")
                .replace("&5", "<dark_purple>")
                .replace("&0", "<black>")
                .replace("&k", "<obfuscated>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>")
        )
}

fun Long.toFriendlyAgoFromMs(): String {
    // If the duration is negative (future timestamp), return "Just now"
    if (this < 0) {
        return "Just now"
    }

    val seconds = TimeUnit.MILLISECONDS.toSeconds(this)
    if (seconds < 60) {
        return "$seconds seconds"
    }
    val minutes = TimeUnit.MILLISECONDS.toMinutes(this)
    if (minutes < 60) {
        return "$minutes minutes"
    }
    val hours = TimeUnit.MILLISECONDS.toHours(this)
    if (hours < 24) {
        return "$hours hours"
    }
    val days = TimeUnit.MILLISECONDS.toDays(this)
    if (days < 30) {
        return "$days days"
    }
    val months = days / 30
    if (months < 12) {
        return "$months months"
    }
    val years = days / 365
    return "$years years"
}

private val plainSerializer = PlainTextComponentSerializer.plainText()

fun Component.toPlain(): String {
    return plainSerializer.serialize(this)
}

fun Component.toMMString(): String {
    return MiniMessage.miniMessage().serialize(this)
}