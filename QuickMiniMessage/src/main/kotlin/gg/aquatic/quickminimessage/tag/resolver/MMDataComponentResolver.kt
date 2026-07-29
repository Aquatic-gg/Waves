package gg.aquatic.quickminimessage.tag.resolver

import gg.aquatic.quickminimessage.tag.MMTagContext
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.DataComponentValue

fun interface MMDataComponentResolver {
    fun resolve(key: Key, value: String, context: MMTagContext): DataComponentValue?

    fun has(key: Key): Boolean = true

    companion object {
        @JvmStatic
        fun empty(): MMDataComponentResolver = EmptyResolver

        @JvmStatic
        fun resolver(handler: (Key, String, MMTagContext) -> DataComponentValue?): MMDataComponentResolver {
            return MMDataComponentResolver { key, value, context -> handler(key, value, context) }
        }
    }

    private object EmptyResolver : MMDataComponentResolver {
        override fun resolve(key: Key, value: String, context: MMTagContext): DataComponentValue? = null
        override fun has(key: Key): Boolean = false
    }
}


