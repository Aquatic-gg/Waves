package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.LocaleProvider
import org.bukkit.plugin.java.JavaPlugin

class PaperLocaleBuilder() {
    var defaultLanguage: String = "en"
    lateinit var provider: LocaleProvider<PaperMessage>

    fun build(): PaperLocaleManager {
        return PaperLocaleManager(
            defaultLanguage = defaultLanguage,
            provider = provider
        )
    }
}

object KLocale {
    fun paper(block: PaperLocaleBuilder.() -> Unit): PaperLocaleManager {
        val builder = PaperLocaleBuilder()
        builder.block()
        return builder.build()
    }
}
