package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.BaseLocaleManager
import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.MissingKeyHandler
import org.bukkit.plugin.java.JavaPlugin

class PaperLocaleBuilder {
    var defaultLanguage: String = "en"
    lateinit var provider: LocaleProvider<PaperMessage>
    var missingKeyHandler: MissingKeyHandler<PaperMessage> = MissingKeyHandler.Throwing()

    fun build(): BaseLocaleManager<PaperMessage> {
        return BaseLocaleManager(
            defaultLanguage = defaultLanguage,
            provider = provider,
            missingKeyHandler = missingKeyHandler
        )
    }
}

object KLocale {
    fun paper(block: PaperLocaleBuilder.() -> Unit): BaseLocaleManager<PaperMessage> {
        val builder = PaperLocaleBuilder()
        builder.block()
        return builder.build()
    }
}