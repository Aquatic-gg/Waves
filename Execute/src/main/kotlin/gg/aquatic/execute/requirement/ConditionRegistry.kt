package gg.aquatic.execute.requirement

import gg.aquatic.execute.Condition
import gg.aquatic.kregistry.*

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>.registerCondition(
    id: String,
    action: Condition<T>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    reg.register(id, action)
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, Condition<*>>>.registerConditions(
    map: Map<String, Condition<T>>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    map.forEach { (id, value) -> reg.register(id, value) }
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> TypedRegistry<String, Condition<*>>.getConditions(): Map<String, Condition<T>> {
    return this[T::class.java]?.getAll() as? Map<String, Condition<T>> ?: emptyMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Condition<*>>.getHierarchical(id: String, clazz: Class<T>): Condition<T>? {
    return (this as TypedRegistry<String, *>).getHierarchical<String, T, Condition<T>>(id, clazz)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, Condition<*>>.getAllHierarchical(clazz: Class<T>): Map<String, Condition<T>> {
    return (this as TypedRegistry<String, *>).getAllHierarchical<String, T, Condition<T>>(clazz)
}
