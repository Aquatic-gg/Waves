package gg.aquatic.klocale.message

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface MessageCache<A : Message<A>> {

    fun get(language: String, key: String): A?
    fun getAll(language: String): Map<String, A>
    fun getAll(): Map<String, Map<String, A>>
    fun getLanguages(): Set<String>

    suspend fun set(map: Map<String, Map<String, A>>)

    class MapCache<A: Message<A>> : MessageCache<A> {

        private var map: MutableMap<String, Map<String,A>> = HashMap()

        override fun get(language: String, key: String): A? {
            return map[language]?.get(key)
        }

        override fun getAll(language: String): Map<String, A> {
            return map[language] ?: emptyMap()
        }

        override fun getAll(): Map<String, Map<String, A>> {
            return map
        }

        override fun getLanguages(): Set<String> {
            return map.keys
        }

        override suspend fun set(map: Map<String, Map<String, A>>) = withContext(Dispatchers.Main) {
            this@MapCache.map = map.toMutableMap()
        }
    }
}