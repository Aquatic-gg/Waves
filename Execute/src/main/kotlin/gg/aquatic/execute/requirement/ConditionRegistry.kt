package gg.aquatic.execute.requirement

import gg.aquatic.execute.Condition
import gg.aquatic.kregistry.*

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>.registerCondition(
    id: String,
    action: Condition<T>
) {
    register<String, Condition<*>, Condition<T>>(id, action)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>.registerConditions(
    map: Map<String, Condition<T>>
) {
    register<String, Condition<*>, Condition<T>>(map)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> TypedRegistry<String, Condition<*>>.getConditions(): Map<String, Condition<T>> {
    return this[T::class.java]?.getAll() as? Map<String, Condition<T>> ?: emptyMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Condition<*>>.getHierarchical(id: String): Condition<T>? {
    return this.getHierarchical<String, Condition<*>, Condition<T>>(id)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Condition<*>>.getAllHierarchical(): Map<String, Condition<T>> {
    return this.getAllHierarchical<String, Condition<*>, Condition<T>>()
}
