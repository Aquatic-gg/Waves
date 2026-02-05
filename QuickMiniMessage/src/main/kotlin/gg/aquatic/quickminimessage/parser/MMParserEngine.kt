package gg.aquatic.quickminimessage.parser

import gg.aquatic.quickminimessage.tag.resolver.MMDataComponentResolver
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component

internal object MMParserEngine {
    @JvmStatic
    fun deserialize(input: String): Component {
        return deserialize(input, MMTagResolver.empty(), MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver): Component {
        return deserialize(input, resolver, MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, dataComponentResolver: MMDataComponentResolver): Component {
        if (input.isEmpty()) {
            return Component.empty()
        }
        if (input.indexOf(TAG_OPEN) < 0 && input.indexOf(ESCAPE) < 0) {
            return Component.text(input)
        }
        return Parser(input, resolver, null, dataComponentResolver).parse()
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, pointered: Pointered?): Component {
        return deserialize(input, resolver, pointered, MMDataComponentResolver.empty())
    }

    @JvmStatic
    fun deserialize(
        input: String,
        resolver: MMTagResolver,
        pointered: Pointered?,
        dataComponentResolver: MMDataComponentResolver
    ): Component {
        if (input.isEmpty()) {
            return Component.empty()
        }
        if (input.indexOf(TAG_OPEN) < 0 && input.indexOf(ESCAPE) < 0) {
            return Component.text(input)
        }
        return Parser(input, resolver, pointered, dataComponentResolver).parse()
    }

    @Suppress("unused")
    @JvmStatic
    fun deserialize(input: String, vararg resolvers: MMTagResolver): Component {
        return deserialize(input, MMTagResolver.resolver(*resolvers))
    }

    @Suppress("unused")
    @JvmStatic
    fun parse(input: String): Component = deserialize(input)
}
