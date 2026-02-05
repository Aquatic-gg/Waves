package gg.aquatic.quickminimessage.tag

import gg.aquatic.quickminimessage.tag.resolver.MMDataComponentResolver
import gg.aquatic.quickminimessage.MMParser
import gg.aquatic.quickminimessage.tag.resolver.MMTagResolver
import net.kyori.adventure.identity.Identity
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.text.Component
import java.util.Locale

class MMTagContext internal constructor(
    private val resolver: MMTagResolver,
    private val pointered: Pointered?,
    private val dataComponentResolver: MMDataComponentResolver
) {
    fun deserialize(input: String): Component {
        return MMParser.deserialize(input, resolver, pointered, dataComponentResolver)
    }

    val locale: Locale?
        get() = pointered?.pointers()?.get(Identity.LOCALE)?.orElse(null)

    val pointeredSource: Pointered?
        get() = pointered
}
