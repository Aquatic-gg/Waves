package gg.aquatic.quickminimessage

import gg.aquatic.quickminimessage.parser.MMParserEngine
import gg.aquatic.quickminimessage.tag.resolver.MMDataComponentResolver
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component

object MMParser {
    @JvmStatic
    fun deserialize(input: String): Component {
        return MMParserEngine.deserialize(input)
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver): Component {
        return MMParserEngine.deserialize(input, resolver)
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, dataComponentResolver: MMDataComponentResolver): Component {
        return MMParserEngine.deserialize(input, resolver, dataComponentResolver)
    }

    @JvmStatic
    fun deserialize(input: String, resolver: MMTagResolver, pointered: Pointered?): Component {
        return MMParserEngine.deserialize(input, resolver, pointered)
    }

    @JvmStatic
    fun deserialize(
        input: String,
        resolver: MMTagResolver,
        pointered: Pointered?,
        dataComponentResolver: MMDataComponentResolver
    ): Component {
        return MMParserEngine.deserialize(input, resolver, pointered, dataComponentResolver)
    }

    @JvmStatic
    fun deserialize(input: String, vararg resolvers: MMTagResolver): Component {
        return MMParserEngine.deserialize(input, *resolvers)
    }

    @JvmStatic
    fun parse(input: String): Component = deserialize(input)
}
