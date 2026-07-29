package gg.aquatic.klocale.provider

import gg.aquatic.klocale.LocaleProvider
import gg.aquatic.klocale.message.Message

class MergedLocaleProvider<M : Message<M>>(
    val providers: List<LocaleProvider<M>>
) : LocaleProvider<M> {
    override suspend fun fetch(): Map<String, Map<String, M>> {
        val finalMap = mutableMapOf<String, MutableMap<String, M>>()

        for (provider in providers) {
            val fetched = provider.fetch()
            fetched.forEach { (lang, messages) ->
                finalMap.getOrPut(lang) { mutableMapOf() }.putAll(messages)
            }
        }
        return finalMap
    }
}