package gg.aquatic.waves.registry

import gg.aquatic.waves.util.generic.Action

inline fun <reified T: Any> WavesRegistry.registerAction(id: String, action: Action<T>) {
    val map = ACTION.getOrPut(T::class.java) { HashMap() }
    map += id to action
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T: Any> WavesRegistry.getAction(id: String): Action<T>? {
    val map = ACTION[T::class.java] ?: return null
    val value = map[id] ?: return null

    return value as? Action<T>?
}

inline fun <reified T: Any> Action<T>.register(id: String) {
    WavesRegistry.registerAction(id, this)
}