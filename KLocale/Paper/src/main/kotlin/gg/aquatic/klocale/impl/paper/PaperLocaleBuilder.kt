package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.BaseLocaleManager
import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.MissingKeyHandler
import net.kyori.adventure.text.minimessage.MiniMessage
import kotlin.io.resolve

class PaperLocaleBuilder {
    var defaultLanguage: String = "en"
    lateinit var provider: LocaleProvider<PaperMessage>
    var missingKeyHandler: MissingKeyHandler<PaperMessage> = MissingKeyHandler.Throwing()
    var miniMessage = Companion.miniMessage

    fun build(): BaseLocaleManager<PaperMessage> {
        Companion.miniMessage = miniMessage
        return BaseLocaleManager(
            defaultLanguage = defaultLanguage,
            provider = provider,
            missingKeyHandler = missingKeyHandler
        )
    }

    companion object {
        var miniMessage = MiniMessage.builder().editTags { b ->
            b.tag("ccmd") { a, b ->
                ConsoleCommandMMResolver.resolve(a, b)
            }
        }.build()
            private set
    }
}

object KLocale {
    fun paper(
        provider: LocaleProvider<PaperMessage>,
        block: PaperLocaleBuilder.() -> Unit
    ): BaseLocaleManager<PaperMessage> {
        val builder = PaperLocaleBuilder()
        builder.provider = provider
        builder.block()
        return builder.build()
    }
}