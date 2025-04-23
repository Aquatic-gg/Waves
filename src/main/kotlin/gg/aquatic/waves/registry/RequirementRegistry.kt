package gg.aquatic.waves.registry

import gg.aquatic.waves.util.generic.Condition

inline fun <reified T: Any> WavesRegistry.registerRequirement(id: String, requirement: Condition<T>) {
    val map = REQUIREMENT.getOrPut(T::class.java) { HashMap() }
    map += id to requirement
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T: Any> WavesRegistry.getRequirement(id: String): Condition<T>? {
    val map = REQUIREMENT[T::class.java] ?: return null
    return map[id] as? Condition<T>
}

inline fun <reified T: Any> Condition<T>.register(id: String) {
    WavesRegistry.registerRequirement(id, this)
}