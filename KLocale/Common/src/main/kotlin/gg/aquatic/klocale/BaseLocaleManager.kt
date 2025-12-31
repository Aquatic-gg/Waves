package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message
import gg.aquatic.klocale.message.MessageCache

open class BaseLocaleManager<A : Message<A>>(
    override val cache: MessageCache<A> = MessageCache.MapCache(),
    override val defaultLanguage: String,
    override val provider: LocaleProvider<A>,
    override val missingKeyHandler: MissingKeyHandler<A> = MissingKeyHandler.Throwing()
) : LocaleManager<A> {

    override suspend fun invalidate() {
        cache.set(provider.fetch())
    }
}