package gg.aquatic.quickminimessage

import net.kyori.adventure.identity.Identity
import net.kyori.adventure.pointer.Pointered
import net.kyori.adventure.pointer.Pointers
import java.util.Locale

class MMLocalePointered internal constructor(locale: Locale) : Pointered {
    private val pointers = Pointers.builder()
        .withStatic(Identity.LOCALE, locale)
        .build()

    override fun pointers(): Pointers = pointers
}