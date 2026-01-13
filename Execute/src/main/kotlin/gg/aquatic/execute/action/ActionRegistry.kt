package gg.aquatic.execute.action

import gg.aquatic.execute.Action
import gg.aquatic.kregistry.*

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>.registerAction(
    id: String,
    action: Action<T>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    reg.register(id, action)
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Action<*>>>.registerActions(
    map: Map<String, Action<T>>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    map.forEach { (id, value) -> reg.register(id, value) }
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> TypedRegistry<String, Action<*>>.getActions(): Map<String, Action<T>> {
    return this[T::class.java]?.getAll() as? Map<String, Action<T>> ?: emptyMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Action<*>>.getHierarchical(id: String, clazz: Class<T>): Action<T>? {
    return (this as TypedRegistry<String, *>).getHierarchical<String, T, Action<T>>(id, clazz)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Action<*>>.getAllHierarchical(clazz: Class<T>): Map<String, Action<T>> {
    return (this as TypedRegistry<String, *>).getAllHierarchical<String, T, Action<T>>(clazz)
}
