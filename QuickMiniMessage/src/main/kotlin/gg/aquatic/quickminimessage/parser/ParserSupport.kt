package gg.aquatic.quickminimessage.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import java.util.*

internal fun transitionColor(colors: Array<TextColor>, phase: Double): TextColor {
    var adjustedPhase = phase.toFloat()
    var reversed = false
    val list = colors.toMutableList()
    if (adjustedPhase < 0f) {
        reversed = true
        adjustedPhase += 1f
        list.reverse()
    }
    val values = list.toTypedArray()
    val steps = 1f / (values.size - 1)
    for (index in 1 until values.size) {
        val point = index * steps
        if (point >= adjustedPhase) {
            val factor = 1 + (adjustedPhase - point) * (values.size - 1)
            return if (reversed) {
                TextColor.lerp(1 - factor, values[index], values[index - 1])
            } else {
                TextColor.lerp(factor, values[index - 1], values[index])
            }
        }
    }
    return values[0]
}

internal fun combine(children: List<Component>): Component {
    return when (children.size) {
        0 -> Component.empty()
        1 -> {
            val child = children[0]
            if (child is TextComponent &&
                child.content().isEmpty() &&
                child.children().isNotEmpty() &&
                child.style().isEmpty()
            ) {
                Component.text()
                    .append(COMPACT_SENTINEL)
                    .append(child)
                    .build()
            } else {
                child
            }
        }
        else -> Component.empty().append(children)
    }
}

internal fun normalizeTagName(name: String): String {
    return when (name) {
        "colour", "c" -> "color"
        "b" -> "bold"
        "em", "i" -> "italic"
        "u" -> "underlined"
        "st" -> "strikethrough"
        "obf" -> "obfuscated"
        "br" -> "newline"
        "sel" -> "selector"
        "tr", "translate" -> "lang"
        "tr_or", "translate_or" -> "lang_or"
        "data" -> "nbt"
        "keybind" -> "key"
        "insertion" -> "insert"
        else -> name
    }
}

internal fun hexValue(ch: Char): Int? {
    return when (ch) {
        in '0'..'9' -> ch.code - '0'.code
        in 'a'..'f' -> ch.code - 'a'.code + 10
        in 'A'..'F' -> ch.code - 'A'.code + 10
        else -> null
    }
}
internal val DECORATIONS = mapOf(
    "bold" to TextDecoration.BOLD,
    "italic" to TextDecoration.ITALIC,
    "underlined" to TextDecoration.UNDERLINED,
    "strikethrough" to TextDecoration.STRIKETHROUGH,
    "obfuscated" to TextDecoration.OBFUSCATED
)

internal val COLOR_ALIASES = mapOf(
    "dark_grey" to NamedTextColor.DARK_GRAY,
    "grey" to NamedTextColor.GRAY
)

internal val CLICK_ACTIONS = ClickEvent.Action.entries.associateBy { it.name.lowercase(Locale.ROOT) }

internal val COMPACT_SENTINEL = Component.text(
    "",
    Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build()
)

internal val DEFAULT_GRADIENT = arrayOf(
    TextColor.color(0xffffff),
    TextColor.color(0x000000)
)

internal val PRIDE_FLAGS = mapOf(
    "pride" to listOf(0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
    "progress" to listOf(0xFFFFFF, 0xFFAFC7, 0x73D7EE, 0x613915, 0x000000, 0xE50000, 0xFF8D00, 0xFFEE00, 0x28121, 0x004CFF, 0x770088),
    "trans" to listOf(0x5BCFFB, 0xF5ABB9, 0xFFFFFF, 0xF5ABB9, 0x5BCFFB),
    "bi" to listOf(0xD60270, 0x9B4F96, 0x0038A8),
    "pan" to listOf(0xFF1C8D, 0xFFD700, 0x1AB3FF),
    "nb" to listOf(0xFCF431, 0xFCFCFC, 0x9D59D2, 0x282828),
    "lesbian" to listOf(0xD62800, 0xFF9B56, 0xFFFFFF, 0xD462A6, 0xA40062),
    "ace" to listOf(0x000000, 0xA4A4A4, 0xFFFFFF, 0x810081),
    "agender" to listOf(0x000000, 0xBABABA, 0xFFFFFF, 0xBAF484, 0xFFFFFF, 0xBABABA, 0x000000),
    "demisexual" to listOf(0x000000, 0xFFFFFF, 0x6E0071, 0xD3D3D3),
    "genderqueer" to listOf(0xB57FDD, 0xFFFFFF, 0x49821E),
    "genderfluid" to listOf(0xFE76A2, 0xFFFFFF, 0xBF12D7, 0x000000, 0x303CBE),
    "intersex" to listOf(0xFFD800, 0x7902AA, 0xFFD800),
    "aro" to listOf(0x3BA740, 0xA8D47A, 0xFFFFFF, 0xABABAB, 0x000000),
    "femboy" to listOf(0xD260A5, 0xE4AFCD, 0xFEFEFE, 0x57CEF8, 0xFEFEFE, 0xE4AFCD, 0xD260A5),
    "baker" to listOf(0xCD66FF, 0xFF6599, 0xFE0000, 0xFE9900, 0xFFFF01, 0x009900, 0x0099CB, 0x350099, 0x990099),
    "philly" to listOf(0x000000, 0x784F17, 0xFE0000, 0xFD8C00, 0xFFE500, 0x119F0B, 0x0644B3, 0xC22EDC),
    "queer" to listOf(0x000000, 0x9AD9EA, 0x00A3E8, 0xB5E51D, 0xFFFFFF, 0xFFC90D, 0xFC6667, 0xFEAEC9, 0x000000),
    "gay" to listOf(0x078E70, 0x26CEAA, 0x98E8C1, 0xFFFFFF, 0x7BADE2, 0x5049CB, 0x3D1A78),
    "bigender" to listOf(0xC479A0, 0xECA6CB, 0xD5C7E8, 0xFFFFFF, 0xD5C7E8, 0x9AC7E8, 0x6C83CF),
    "demigender" to listOf(0x7F7F7F, 0xC3C3C3, 0xFBFF74, 0xFFFFFF, 0xFBFF74, 0xC3C3C3, 0x7F7F7F)
).mapValues { (_, colors) -> colors.map(TextColor::color) }

internal const val DEFAULT_SHADOW_ALPHA = 0.25f
internal const val TAG_OPEN = '<'
internal const val TAG_CLOSE = '>'
internal const val ESCAPE = '\\'
internal const val SINGLE_QUOTE = '\''
internal const val DOUBLE_QUOTE = '"'
internal const val ARG_SEPARATOR = ':'

