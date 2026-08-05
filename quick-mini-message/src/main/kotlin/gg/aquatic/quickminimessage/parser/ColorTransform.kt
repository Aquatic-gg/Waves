package gg.aquatic.quickminimessage.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
internal object ColorTransform {
    fun sizeOf(component: Component): Int {
        var size = 0
        traverse(component) { current ->
            if (current is TextComponent) {
                val content = current.content()
                if (content.isNotEmpty()) {
                    size += content.codePointCount(0, content.length)
                }
            } else {
                size += 1
            }
        }
        return size
    }

    fun apply(component: Component, colorizer: Colorizer): Component {
        return applyInternal(component, colorizer)
    }

    private fun applyInternal(component: Component, colorizer: Colorizer): Component {
        if (component.style().color() != null) {
            if (component is TextComponent) {
                val content = component.content()
                if (content.isNotEmpty()) {
                    advanceBy(content, colorizer)
                }
            }
            return component
        }
        return when (component) {
            is TextComponent -> applyToText(component, colorizer)
            else -> applyToOther(component, colorizer)
        }
    }

    private fun applyToText(component: TextComponent, colorizer: Colorizer): Component {
        val content = component.content()
        val children = component.children()
        if (content.isEmpty() && children.isEmpty()) {
            return component
        }
        val builder = Component.text()
        if (content.isNotEmpty()) {
            val style = component.style()
            val holder = IntArray(1)
            val iterator = content.codePoints().iterator()
            while (iterator.hasNext()) {
                holder[0] = iterator.nextInt()
                builder.append(Component.text(String(holder, 0, 1), style.color(colorizer.color())))
                colorizer.advance()
            }
        }
        if (children.isNotEmpty()) {
            for (child in children) {
                builder.append(applyInternal(child, colorizer))
            }
        }
        return builder.build()
    }

    private fun applyToOther(component: Component, colorizer: Colorizer): Component {
        val colored = component.colorIfAbsent(colorizer.color())
        colorizer.advance()
        val children = component.children()
        if (children.isEmpty()) {
            return colored
        }
        val updatedChildren = ArrayList<Component>(children.size)
        for (child in children) {
            updatedChildren.add(applyInternal(child, colorizer))
        }
        return colored.children(updatedChildren)
    }

    private fun traverse(component: Component, visitor: (Component) -> Unit) {
        visitor(component)
        for (child in component.children()) {
            traverse(child, visitor)
        }
    }

    private fun advanceBy(content: String, colorizer: Colorizer) {
        val count = content.codePointCount(0, content.length)
        repeat(count) {
            colorizer.advance()
        }
    }
}

internal data class ColorListResult(val colors: Array<TextColor>, val phase: Double) {
    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ColorListResult) {
            return false
        }
        if (!colors.contentEquals(other.colors)) {
            return false
        }
        return phase == other.phase
    }

    override fun hashCode(): Int {
        var result = colors.contentHashCode()
        result = 31 * result + phase.hashCode()
        return result
    }
}

