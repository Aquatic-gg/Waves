package gg.aquatic.waves.registry

import gg.aquatic.waves.util.price.AbstractPrice

inline fun <reified T: Any> WavesRegistry.registerPrice(id: String, price: AbstractPrice<T>) {
    val map = PRICE.getOrPut(T::class.java) { HashMap() }
    map += id to price
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T: Any> WavesRegistry.getPrice(id: String): AbstractPrice<T>? {
    val map = PRICE[T::class.java] ?: return null
    return map[id] as AbstractPrice<T>
}

inline fun <reified T: Any> AbstractPrice<T>.register(id: String) {
    WavesRegistry.registerPrice(id, this)
}