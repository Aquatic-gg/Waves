package gg.aquatic.statistik

import gg.aquatic.kregistry.*

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, StatisticType<*>>>.registerStatistic(
    id: String,
    action: StatisticType<T>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    reg.register(id, action)
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> MutableRegistry<Class<*>, FrozenRegistry<String, StatisticType<*>>>.registerStatistics(
    map: Map<String, StatisticType<T>>
) {
    val reg = this[T::class.java]?.unfreeze() ?: MutableRegistry()
    map.forEach { (id, value) -> reg.register(id, value) }
    this.register(T::class.java, reg.freeze())
}

@Suppress("UNCHECKED_CAST")
inline fun <reified T : Any> TypedRegistry<String, StatisticType<*>>.getStatistics(): Map<String, StatisticType<T>> {
    return this[T::class.java]?.getAll() as? Map<String, StatisticType<T>> ?: emptyMap()
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, StatisticType<*>>.getHierarchical(id: String, clazz: Class<T>): StatisticType<T>? {
    return (this as TypedRegistry<String, *>).getHierarchical<String, T, StatisticType<T>>(id, clazz)
}

@Suppress("UNCHECKED_CAST")
fun <T : Any> TypedRegistry<String, StatisticType<*>>.getAllHierarchical(clazz: Class<T>): Map<String, StatisticType<T>> {
    return (this as TypedRegistry<String, *>).getAllHierarchical<String, T, StatisticType<T>>(clazz)
}
