package gg.aquatic.quickminimessage.argument

import gg.aquatic.quickminimessage.tag.MMTag
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import gg.aquatic.quickminimessage.translator.MMTranslatorTarget
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.TranslationArgumentLike

@Suppress("unused")
object MMArgument {
    @JvmStatic
    fun bool(name: String, value: Boolean): ComponentLike {
        return argument(name, TranslationArgument.bool(value))
    }

    @JvmStatic
    fun numeric(name: String, value: Number): ComponentLike {
        return argument(name, TranslationArgument.numeric(value))
    }

    @JvmStatic
    fun numeric(name: String, value: String): ComponentLike {
        return string(name, value)
    }

    @JvmStatic
    fun string(name: String, value: String): ComponentLike {
        return argument(name, TranslationArgument.component(Component.text(value)))
    }

    @JvmStatic
    fun component(name: String, value: ComponentLike): ComponentLike {
        return argument(name, TranslationArgument.component(value))
    }

    @JvmStatic
    fun argument(name: String, value: TranslationArgumentLike): ComponentLike {
        return argument(name, value.asTranslationArgument())
    }

    @JvmStatic
    fun argument(name: String, value: TranslationArgument): ComponentLike {
        return Component.virtual(Void::class.java, MMTranslatorArgument(name, value))
    }

    @JvmStatic
    fun tag(name: String, tag: MMTag): ComponentLike {
        return Component.virtual(Void::class.java, MMTranslatorArgument(name, tag))
    }

    @JvmStatic
    fun tagResolver(vararg resolvers: MMTagResolver): ComponentLike {
        return tagResolver(MMTagResolver.resolver(*resolvers))
    }

    @JvmStatic
    fun tagResolver(resolvers: Iterable<MMTagResolver>): ComponentLike {
        return tagResolver(MMTagResolver.resolver(*resolvers.toList().toTypedArray()))
    }

    @JvmStatic
    fun tagResolver(resolver: MMTagResolver): ComponentLike {
        return Component.virtual(Void::class.java, MMTranslatorArgument("unused", resolver))
    }

    @JvmStatic
    fun target(pointered: Pointered): ComponentLike {
        return Component.virtual(Void::class.java, MMTranslatorTarget(pointered))
    }
}