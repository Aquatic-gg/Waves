package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.LocaleManager
import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.impl.paper.handler.CfgMessageHandler
import gg.aquatic.klocale.impl.paper.provider.YamlLocaleProvider
import java.io.File
import java.util.Locale

enum class MessagesExample(
    override val path: String
) : CfgMessageHandler<PaperMessage> {

    EXAMPLE("example");

    override fun message(locale: Locale): PaperMessage {
        return manager.getOrThrow(locale, path)
    }

    override val manager: LocaleManager<PaperMessage>
        get() = TODO("Not yet implemented")


    companion object {
        private val provider: LocaleProvider<PaperMessage> = YamlLocaleProvider(
            File("locale.yml"),
            YamlLocaleProvider.DefaultSerializer
        )

        private var injected = false

        suspend fun load(manager: LocaleManager<PaperMessage>) {
            if (!injected) {
                injected = true
                manager.injectProvider(provider)
                return
            }

            manager.invalidate()
        }
    }
}