package gg.aquatic.quickminimessage.tag

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.StyleBuilderApplicable

sealed interface MMTag {
    data class Inserting(val component: Component, val allowsChildren: Boolean) : MMTag
    data class Styling(val style: Style) : MMTag
    data class PreProcess(val value: String) : MMTag

    companion object {
        @JvmStatic
        fun inserting(component: ComponentLike): MMTag = Inserting(component.asComponent(), true)

        @JvmStatic
        fun selfClosingInserting(component: ComponentLike): MMTag = Inserting(component.asComponent(), false)

        @JvmStatic
        fun styling(style: Style): MMTag = Styling(style)

        @JvmStatic
        fun styling(vararg applicable: StyleBuilderApplicable): MMTag {
            val builder = Style.style()
            for (item in applicable) {
                item.styleApply(builder)
            }
            return Styling(builder.build())
        }

        @JvmStatic
        fun preProcessParsed(value: String): MMTag = PreProcess(value)
    }
}