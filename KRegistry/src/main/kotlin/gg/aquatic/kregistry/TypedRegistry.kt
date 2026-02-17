package gg.aquatic.kregistry

import kotlin.collections.iterator

interface GenericTyped<T : Any> {
    val type: Class<out T>
}

typealias TypedRegistry<Id, Type> = Registry<Class<*>, Registry<Id, out Type>>

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> Registry<Class<*>, *>.getTyped(
    id: Id,
    clazz: Class<Clazz>
): Type? {
    val reg = this.get(clazz) as? Registry<Id, *> ?: return null
    return reg.get(id) as? Type
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Clazz> Registry<Class<*>, *>.getTyped(id: Any): Clazz? {
    val reg = this.get(Clazz::class.java) as? Registry<Any, *> ?: return null
    return reg.get(id) as? Clazz
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Clazz> Registry<Class<*>, *>.getTypedAll(): Map<Any, Clazz>? {
    val reg = this.get(Clazz::class.java) as? Registry<Any, *> ?: return null
    return reg.all() as? Map<Any, Clazz>
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> Registry<Class<*>, *>.getHierarchical(
    id: Id,
    clazz: Class<Clazz>
): Type? {
    val typed = getTyped<Id, Clazz, Type>(id, clazz)
    if (typed != null) return typed

    val allData = getAllHierarchical<Id, Clazz, Type>(clazz)
    return allData[id]
}

inline fun <reified Clazz> Registry<Class<*>, *>.getHierarchical(id: Any): Clazz? {
    val typed = getTyped<Clazz>(id)
    if (typed != null) return typed

    val allData = getAllHierarchical<Clazz>()
    return allData[id]
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> Registry<Class<*>, *>.getAllHierarchical(
    clazz: Class<Clazz>
): Map<Id, Type> {
    val rawMap = this.all()
    return buildMap {
        for ((entryClass, reg) in rawMap) {
            if (clazz.isAssignableFrom(entryClass)) {
                putAll((reg as Registry<Id, Type>).all())
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Clazz> Registry<Class<*>, *>.getAllHierarchical(): Map<Any, Clazz> {
    val rawMap = this.all()
    return buildMap {
        for ((entryClass, reg) in rawMap) {
            if (Clazz::class.java.isAssignableFrom(entryClass)) {
                putAll((reg as Registry<Any, Clazz>).all())
            }
        }
    }
}

typealias TypedCollectionRegistry<T> = Registry<Class<out T>, List<GenericTyped<out T>>>

fun <T : Any> TypedCollectionRegistry<T>.getTypedEntries(
    clazz: Class<out T>
): List<GenericTyped<out T>> {
    return this.get(clazz) ?: emptyList()
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Clazz> Registry<*, *>.getTypedEntries(): List<GenericTyped<*>> {
    val reg = this as? Registry<Class<*>, *> ?: return emptyList()
    return reg.get(Clazz::class.java) as? List<GenericTyped<*>> ?: emptyList()
}

fun <T : Any> TypedCollectionRegistry<T>.getAllHierarchicalEntries(
    clazz: Class<out T>
): List<GenericTyped<out T>> {
    val rawMap = this.all()
    val result = ArrayList<GenericTyped<out T>>()
    for ((entryClass, entries) in rawMap) {
        if (clazz.isAssignableFrom(entryClass)) {
            result += entries
        }
    }
    return result
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Clazz> Registry<*, *>.getAllHierarchicalEntries(): List<GenericTyped<*>> {
    val reg = this as? Registry<Class<*>, *> ?: return emptyList()
    val rawMap = reg.all()
    val result = ArrayList<GenericTyped<*>>()
    for ((entryClass, entries) in rawMap) {
        if (Clazz::class.java.isAssignableFrom(entryClass)) {
            result += entries as List<GenericTyped<*>>
        }
    }
    return result
}

fun <T : Any> TypedCollectionRegistry<T>.getHierarchicalEntries(
    clazz: Class<out T>
): List<GenericTyped<out T>> {
    return getAllHierarchicalEntries(clazz)
}

inline fun <reified Clazz> Registry<*, *>.getHierarchicalEntries(): List<GenericTyped<*>> {
    return getAllHierarchicalEntries<Clazz>()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> RegistryContributionBuilder<Class<out T>, List<GenericTyped<out T>>>.addTyped(value: GenericTyped<out T>) {
    val key = value.type
    val existing = data[key] as? MutableList<GenericTyped<out T>> ?: ArrayList()
    existing += value
    data[key] = existing
}
