package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.BaseLocaleManager
import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.MissingKeyHandler
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.Locale
import kotlin.io.resolve

class PaperLocaleBuilder {
    var defaultLanguage: Locale = Locale.ENGLISH
    var missingKeyHandler: MissingKeyHandler<PaperMessage> = MissingKeyHandler.Throwing()
    var miniMessage = Companion.miniMessage
    var providers: MutableIterable<LocaleProvider<PaperMessage>> = mutableListOf()

    fun build(): BaseLocaleManager<PaperMessage> {
        Companion.miniMessage = miniMessage
        return BaseLocaleManager(
            defaultLanguage = defaultLanguage,
            providers = providers,
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
        block: PaperLocaleBuilder.() -> Unit
    ): BaseLocaleManager<PaperMessage> {
        val builder = PaperLocaleBuilder()
        builder.block()
        return builder.build()
    }
}