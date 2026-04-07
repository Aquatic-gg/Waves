package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message
import gg.aquatic.klocale.message.MessageCache
import java.util.*

open class BaseLocaleManager<A : Message<A>>(
    override val cache: MessageCache<A> = MessageCache.MapCache(),
    override val defaultLanguage: Locale,
    protected val mutableProviders: MutableCollection<LocaleProvider<A>>,
    override val missingKeyHandler: MissingKeyHandler<A> = MissingKeyHandler.Throwing()
) : LocaleManager<A> {

    override val providers: Iterable<LocaleProvider<A>>
        get() = mutableProviders

    override suspend fun invalidate() {
        val map = HashMap<String, Map<String, A>>()
        for (provider in mutableProviders) {
            map += provider.fetch()
        }
        cache.set(map)
    }

    override suspend fun injectProvider(provider: LocaleProvider<A>) {
        mutableProviders.add(provider)
        cache.inject(provider.fetch())
    }

    override fun removeProvider(provider: LocaleProvider<A>) {
        mutableProviders.remove(provider)
    }

    override fun clearProviders() {
        mutableProviders.clear()
    }
}
