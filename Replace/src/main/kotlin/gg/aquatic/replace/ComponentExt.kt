package gg.aquatic.replace

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent

val PLACEHOLDER_REGEX = Regex("%([^%]+)%")

fun Component.findPlaceholders(): Set<String> {
    val result = mutableSetOf<String>()

    fun recurse(comp: Component) {
        if (comp is TextComponent) {
            val text = comp.content()
            PLACEHOLDER_REGEX.findAll(text).forEach { match ->
                result.add(match.groupValues[1]) // only the inside of %...%
            }
        }
        for (child in comp.children()) {
            recurse(child)
        }
    }

    recurse(this)
    return result
}

fun Component.replacePlaceholders(updater: (String) -> String): Component {
    fun recurse(comp: Component): Component {
        val style = comp.style()
        var newStyle = style

        // Handle hover event if present
        style.hoverEvent()?.let { hover ->
            if (hover.action() == HoverEvent.Action.SHOW_TEXT) {
                val hoverComp = hover.value() as? Component
                if (hoverComp != null) {
                    val replacedHover = recurse(hoverComp)
                    newStyle = newStyle.hoverEvent(HoverEvent.showText(replacedHover))
                }
            }
        }

        // Handle insertion text (plain string)
        style.insertion()?.let { insertion ->
            val newInsertion = updater(insertion)
            newStyle = newStyle.insertion(newInsertion)
        }

        var newComponent = if (comp is TextComponent) {
            val newText = updater(comp.content())
            val rebuilt = Component.text(newText).style(newStyle)
            rebuilt
        } else {
            val rebuilt: Component = comp.style(newStyle)
            rebuilt
        }
        for (child in comp.children()) {
            newComponent = newComponent.append(recurse(child))
        }
        return newComponent
    }
    return recurse(this)
}

/**
 * Only use when we 100 know that we got predefined placeholders
 */
fun Component.replacePlaceholders(cached: Map<String, String>): Component {
    fun recurse(comp: Component): Component {
        val style = comp.style()
        var newStyle = style

        // Handle hover event if present
        style.hoverEvent()?.let { hover ->
            if (hover.action() == HoverEvent.Action.SHOW_TEXT) {
                val hoverComp = hover.value() as? Component
                if (hoverComp != null) {
                    val replacedHover = recurse(hoverComp)
                    newStyle = newStyle.hoverEvent(HoverEvent.showText(replacedHover))
                }
            }
        }

        // Handle insertion text (plain string)
        style.insertion()?.let { insertion ->
            var newInsertion = insertion
            PLACEHOLDER_REGEX.findAll(insertion).forEach { match ->
                val key = match.groupValues[1]
                newInsertion = insertion.replace("%$key%", cached[key] ?: key)
            }
            newStyle = newStyle.insertion(newInsertion)
        }

        var newComponent = if (comp is TextComponent) {
            var newText = comp.content()
            PLACEHOLDER_REGEX.findAll(newText).forEach { match ->
                val key = match.groupValues[1]
                newText = newText.replace("%$key%", cached[key] ?: key)
            }
            val rebuilt = Component.text(newText).style(newStyle)
            rebuilt
        } else {
            val rebuilt: Component = comp.style(newStyle)
            rebuilt
        }
        for (child in comp.children()) {
            newComponent = newComponent.append(recurse(child))
        }
        return newComponent
    }
    return recurse(this)
}

fun Component.containsPlaceholder(): Boolean {
    fun recurse(comp: Component): Boolean {
        if (comp is TextComponent) {
            if (PLACEHOLDER_REGEX.containsMatchIn(comp.content())) {
                return true
            }
        }
        for (child in comp.children()) {
            if (recurse(child)) return true
        }
        return false
    }
    return recurse(this)
}

fun String.hasPlaceholder(): Boolean {
    return PLACEHOLDER_REGEX.containsMatchIn(this)
}