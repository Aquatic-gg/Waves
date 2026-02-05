package gg.aquatic.quickminimessage

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.StyleBuilderApplicable

@Suppress("unused")
object MMPlaceholder {
    @JvmStatic
    fun parsed(name: String, value: String): MMTagResolver {
        return MMTagResolver.resolver(name, MMTag.preProcessParsed(value))
    }

    @JvmStatic
    fun unparsed(name: String, value: String): MMTagResolver {
        return MMTagResolver.resolver(name, MMTag.selfClosingInserting(Component.text(value)))
    }

    @JvmStatic
    fun component(name: String, component: ComponentLike): MMTagResolver {
        return MMTagResolver.resolver(name, MMTag.selfClosingInserting(component))
    }

    @JvmStatic
    fun styling(name: String, vararg applicable: StyleBuilderApplicable): MMTagResolver {
        return MMTagResolver.resolver(name, MMTag.styling(*applicable))
    }
}
