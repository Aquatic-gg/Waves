package gg.aquatic.replace

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.Style

val PLACEHOLDER_REGEX = Regex("%([^%]+)%")

/**
 * Recursively searches the current component and its children for placeholders defined within
 * `%...%` and extracts their names.
 *
 * @return A set of placeholder names found within the component's content and its children.
 */
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

/**
 * Replaces placeholders within a `Component` by recursively traversing its structure and applying
 * the provided `updater` function. This method handles text content, hover events, and insertion
 * strings within the component's style.
 *
 * @param updater A lambda function that takes a `String` as input and returns the updated string.
 *                This function is used to replace or transform text throughout the component.
 * @return A new `Component` with placeholders replaced according to the `updater` function.
 */
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
 * Only use when we 100% know that we got predefined placeholders
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

/**
 * Checks if the component or any of its child components contains a placeholder.
 *
 * A placeholder is identified based on the presence of a specific pattern
 * defined by the `PLACEHOLDER_REGEX`.
 *
 * @return `true` if the component or any of its children contains a placeholder, `false` otherwise.
 */
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

/**
 * Replaces certain substrings or components of this `Component` instance with the corresponding components
 * provided in the given mapping. This process is recursive and applies to both the main component content
 * and its hover events (if any), as well as to all child components.
 *
 * @param map A map where the key represents the substring to match within the component,
 * and the value is a lambda that generates the `Component` to replace the matching substring.
 * @return A new `Component` instance with the specified replacements applied. The original component remains unchanged.
 */
fun Component.replaceWith(map: Map<String, () -> Component>): Component {
    fun recurseSplit(mapLeft: Map<String, () -> Component>, str: String, style: Style): Component {
        var component = Component.text()
        val left = mapLeft.toMutableMap()

        var found = false
        mapLeft.forEach { (key, value) ->
            if (str.contains(key)) {
                left -= key

                val split = str.split(key)

                for ((index,string) in split.withIndex()) {
                    component = component.append(recurseSplit(left.toMutableMap(), string, style))
                    if (index < split.size - 1) {
                        component = component.append(value)
                    }
                }
                found = true
            }
        }
        if (!found) {
            component = component.append(Component.text(str, style))
        }
        return component.build()
    }

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

        var newComponent = if (comp is TextComponent) {
            recurseSplit(map, comp.content(), newStyle)
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