package gg.aquatic.klocale

import gg.aquatic.klocale.message.Message
import gg.aquatic.klocale.message.MessageCache
import java.util.Locale

interface LocaleManager<A : Message<A>> {

    val defaultLanguage: String
    val cache: MessageCache<A>
    val provider: LocaleProvider<A>

    suspend fun invalidate()

    fun get(language: Locale, key: String): A? {
        return cache.get(language.language, key)
    }

    fun getOrDefault(language: Locale, key: String): A {
        val msg =
            cache.get(language.language, key) ?: if (defaultLanguage == language.language) null else cache.get(defaultLanguage, key)
        return msg ?: throw IllegalArgumentException("Message $key not found for language $language")
    }

    fun getOrThrow(language: Locale, key: String): A {
        return get(language, key) ?: throw IllegalArgumentException("Message $key not found for language $language")
    }

    fun getAll(language: Locale): Map<String, A> {
        return cache.getAll(language.language)
    }

    fun getAll(): Map<String, Map<String, A>> {
        return cache.getAll()
    }

    fun getLanguages(): Set<String> {
        return cache.getLanguages()
    }
}