package gg.aquatic.execute.action

import gg.aquatic.execute.Action
import gg.aquatic.kregistry.*

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>.registerAction(
    id: String,
    action: Action<T>
) {
    register<String, Action<*>, Action<T>>(id, action)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>.registerActions(
    map: Map<String, Action<T>>
) {
    register<String, Action<*>, Action<T>>(map)
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> TypedRegistry<String, Action<*>>.getActions(): Map<String, Action<T>> {
    return this[T::class.java]?.getAll() as? Map<String, Action<T>> ?: emptyMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Action<*>>.getHierarchical(id: String): Action<T>? {
    return this.getHierarchical<String, Action<*>, Action<T>>(id)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Action<*>>.getAllHierarchical(): Map<String, Action<T>> {
    return this.getAllHierarchical<String, Action<*>, Action<T>>()
}
