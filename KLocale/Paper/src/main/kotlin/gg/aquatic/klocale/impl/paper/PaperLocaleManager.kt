package gg.aquatic.klocale.impl.paper

import gg.aquatic.klocale.LocaleManager
import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.message.MessageCache

class PaperLocaleManager(
    override val cache: MessageCache<PaperMessage> = MessageCache.MapCache(),
    override val defaultLanguage: String,
    override val provider: LocaleProvider<PaperMessage>
) : LocaleManager<PaperMessage> {

    override suspend fun invalidate() {
        cache.set(provider.fetch())
    }
}