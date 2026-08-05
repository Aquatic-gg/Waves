package gg.aquatic.quickminimessage.parser

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
internal sealed class Frame(val tagName: String?) {
    val children = ArrayList<Component>(4)
    abstract fun build(): Component
}

internal class RootFrame : Frame(null) {
    override fun build(): Component = combine(children)
}

internal class StyleFrame(tagName: String, private val style: Style) : Frame(tagName) {
    override fun build(): Component {
        if (children.isEmpty()) {
            return Component.text("", style)
        }
        val builder = Component.text().style(style)
        for (child in children) {
            builder.append(child)
        }
        return builder.build()
    }
}

internal class InsertFrame(tagName: String, private val base: Component) : Frame(tagName) {
    override fun build(): Component {
        if (children.isEmpty()) {
            return base
        }
        return base.append(children)
    }
}

internal class ColorFrame(tagName: String, private val colorizer: Colorizer) : Frame(tagName) {
    override fun build(): Component {
        val combined = combine(children)
        val size = ColorTransform.sizeOf(combined)
        if (size <= 0) {
            return combined
        }
        colorizer.init(size)
        return ColorTransform.apply(combined, colorizer)
    }
}

