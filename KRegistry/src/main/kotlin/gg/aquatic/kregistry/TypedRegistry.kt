package gg.aquatic.kregistry

import kotlin.collections.iterator

typealias TypedRegistry<Id, Type> = FrozenRegistry<Class<*>, FrozenRegistry<Id, Type>>

inline fun <Id, Type, reified T : Type> TypedRegistry<Id, Type>.getTyped(id: Id): T? {
    return this[T::class.java]?.get(id) as? T
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Type, reified T : Type> TypedRegistry<Id, Type>.getTypedAll(): Map<Id, T>? {
    return this[T::class.java]?.getAll() as? Map<Id, T>
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Type, reified T : Type> TypedRegistry<Id, Type>.getHierarchical(id: Id): T? {
    val typed = getTyped<Id, Type, T>(id)
    if (typed != null) return typed

    val allData = getAllHierarchical<Id, Type, T>()
    return allData[id]
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Type, reified T : Type> TypedRegistry<Id, Type>.getAllHierarchical(): Map<Id, T> {
    val type = T::class.java
    val rawMap = this.getAll()

    return buildMap {
        for ((clazz, reg) in rawMap) {
            if (type.isAssignableFrom(clazz)) {
                putAll(reg.getAll() as Map<Id, T>)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Type, reified T : Type> MutableRegistry<Class<*>, FrozenRegistry<Id, Type>>.register(
    id: Id,
    value: T
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    reg.register(id, value)
    this.register(T::class.java, reg.freeze())
}


@Suppress("UNCHECKED_CAST")
inline fun <Id, Type, reified T : Type> MutableRegistry<Class<*>, FrozenRegistry<Id, Type>>.register(
    map: Map<Id, T>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    map.forEach { (id, value) -> reg.register(id, value) }
    this.register(T::class.java, reg.freeze())
}