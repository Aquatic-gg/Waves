package gg.aquatic.kregistry.core

import gg.aquatic.kregistry.bootstrap.RegistryContributionBuilder
import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey

interface GenericTyped<T : Any> {
    val type: Class<out T>
}

interface ValueTyped<T : Any> : GenericTyped<T> {
    val value: T
    override val type: Class<out T> get() = value::class.java
}

data class SimpleTyped<T : Any>(override val value: T) : ValueTyped<T>

typealias TypedRegistry<Id, Type> = Registry<Class<*>, Registry<Id, out Type>>
typealias GroupedRegistry<Id, Group, Value> = Registry<Class<out Group>, Registry<Id, Value>>

@Suppress("UNCHECKED_CAST")
fun <Id, Group, Type> TypedRegistry<Id, Type>.getTypedByClass(
    id: Id,
    clazz: Class<Group>
): Group? where Group : Type {
    val reg = this.get(clazz as Class<*>) as? Registry<Id, *> ?: return null
    return reg.get(id) as? Group
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Group, Id, Type> TypedRegistry<Id, Type>.getTyped(id: Id): Group? where Group : Type {
    return getTypedByClass(id, Group::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <Id, Group, Type> TypedRegistry<Id, Type>.getTypedAllByClass(clazz: Class<Group>): Map<Id, Group>?
    where Group : Type {
    val reg = this.get(clazz as Class<*>) as? Registry<Id, *> ?: return null
    return reg.all() as? Map<Id, Group>
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Group, Id, Type> TypedRegistry<Id, Type>.getTypedAll(): Map<Id, Group>? where Group : Type {
    return getTypedAllByClass(Group::class.java)
}

@Suppress("UNCHECKED_CAST")
fun <Id, Group, Type> TypedRegistry<Id, Type>.getHierarchicalByClass(
    id: Id,
    clazz: Class<Group>
): Group? where Group : Type {
    val typed = getTypedByClass(id, clazz)
    if (typed != null) return typed

    val allData = getAllHierarchicalByClass(clazz)
    return allData[id]
}

inline fun <reified Group, Id, Type> TypedRegistry<Id, Type>.getHierarchical(id: Id): Group?
    where Group : Type {
    val typed = getTyped<Group, Id, Type>(id)
    if (typed != null) return typed

    val allData = getAllHierarchical<Group, Id, Type>()
    return allData[id]
}

@Suppress("UNCHECKED_CAST")
fun <Id, Group, Type> TypedRegistry<Id, Type>.getAllHierarchicalByClass(clazz: Class<Group>): Map<Id, Group>
    where Group : Type {
    val rawMap = this.all()
    return buildMap {
        for ((entryClass, reg) in rawMap) {
            if (clazz.isAssignableFrom(entryClass)) {
                putAll((reg as Registry<Id, Group>).all())
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <reified Group, Id, Type> TypedRegistry<Id, Type>.getAllHierarchical(): Map<Id, Group>
    where Group : Type {
    return getAllHierarchicalByClass(Group::class.java)
}

@JvmName("getGroupedTypedByClass")
@Suppress("UNCHECKED_CAST")
fun <Id, Group, Value> GroupedRegistry<Id, Group, Value>.getTypedByClass(
    id: Id,
    clazz: Class<out Group>
): Value? {
    val reg = this.get(clazz) as? Registry<Id, *> ?: return null
    return reg.get(id) as? Value
}

@JvmName("getGroupedTyped")
inline fun <Id, reified Group, Value> GroupedRegistry<Id, Group, Value>.getTyped(id: Id): Value? {
    return getTypedByClass(id, Group::class.java)
}

@JvmName("getGroupedHierarchicalByClass")
@Suppress("UNCHECKED_CAST")
fun <Id, Group, Value> GroupedRegistry<Id, Group, Value>.getHierarchicalByClass(
    id: Id,
    clazz: Class<out Group>
): Value? {
    val typed = getTypedByClass(id, clazz)
    if (typed != null) return typed

    val allData = getAllHierarchicalByClass(clazz)
    return allData[id]
}

@JvmName("getGroupedHierarchical")
inline fun <Id, reified Group, Value> GroupedRegistry<Id, Group, Value>.getHierarchical(id: Id): Value? {
    return getHierarchicalByClass(id, Group::class.java)
}

@JvmName("getGroupedAllHierarchicalByClass")
@Suppress("UNCHECKED_CAST")
fun <Id, Group, Value> GroupedRegistry<Id, Group, Value>.getAllHierarchicalByClass(
    clazz: Class<out Group>
): Map<Id, Value> {
    val rawMap = this.all()
    return buildMap {
        for ((entryClass, reg) in rawMap) {
            if (clazz.isAssignableFrom(entryClass)) {
                putAll((reg as Registry<Id, Value>).all())
            }
        }
    }
}

@JvmName("getGroupedAllHierarchical")
inline fun <Id, reified Group, Value> GroupedRegistry<Id, Group, Value>.getAllHierarchical(): Map<Id, Value> {
    return getAllHierarchicalByClass(Group::class.java)
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

fun <Id, Group, Value> RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>.addGrouped(
    group: Class<out Group>,
    registryKey: RegistryKey<Id, Value>,
    builder: RegistryContributionBuilder<Id, Value>.() -> Unit
) {
    val innerBuilder = RegistryContributionBuilder<Id, Value>()
    innerBuilder.builder()
    add(group, Registry(registryKey, innerBuilder.data, emptyMap()))
}

fun <Id, Group, Value> RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>.addGrouped(
    group: Class<out Group>,
    registryId: RegistryId,
    builder: RegistryContributionBuilder<Id, Value>.() -> Unit
) {
    addGrouped(group, RegistryKey(registryId), builder)
}

inline fun <reified Group, Id, Value> RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>.addGrouped(
    registryKey: RegistryKey<Id, Value>,
    noinline builder: RegistryContributionBuilder<Id, Value>.() -> Unit
) {
    addGrouped(Group::class.java, registryKey, builder)
}

inline fun <reified Group, Id, Value> RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>.addGrouped(
    registryId: RegistryId,
    noinline builder: RegistryContributionBuilder<Id, Value>.() -> Unit
) {
    addGrouped(Group::class.java, RegistryKey(registryId), builder)
}
