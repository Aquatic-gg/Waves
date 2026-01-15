package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message
import gg.aquatic.klocale.message.MessageCache

open class BaseLocaleManager<A : Message<A>>(
    override val cache: MessageCache<A> = MessageCache.MapCache(),
    override val defaultLanguage: String,
    override val providers: MutableIterable<LocaleProvider<A>>,
    override val missingKeyHandler: MissingKeyHandler<A> = MissingKeyHandler.Throwing()
) : LocaleManager<A> {

    override suspend fun invalidate() {
        val map = HashMap<String, Map<String, A>>()
        for (provider in providers) {
            map += provider.fetch()
        }
        cache.set(map)
    }

    override suspend fun injectProvider(provider: LocaleProvider<A>) {
        providers.plus(provider)
        cache.inject(provider.fetch())
    }

    override fun removeProvider(provider: LocaleProvider<A>) {
        providers.minus(provider)
    }

    override fun clearProviders() {
        providers.removeAll { true }
    }
}