package gg.aquatic.kregistry

typealias TypedRegistry<Id, Type> = FrozenRegistry<Class<*>, FrozenRegistry<Id, Type>>

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> TypedRegistry<Id, *>.getTyped(id: Id, clazz: Class<Clazz>): Type? {
    return (this as TypedRegistry<Id, Type>)[clazz]?.get(id)
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, reified Clazz, reified Type> TypedRegistry<Id, *>.getTypedAll(): Map<Id, Type>? {
    return this[Clazz::class.java]?.getAll() as? Map<Id, Type>
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> TypedRegistry<Id, *>.getHierarchical(id: Id, clazz: Class<Clazz>): Type? {
    //val ignored = this as TypedRegistry<Id, Type>
    val typed = getTyped<Id, Clazz, Type>(id, clazz)
    if (typed != null) return typed

    val allData = getAllHierarchical<Id, Clazz, Type>(clazz)
    return allData[id]
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, Clazz, reified Type> TypedRegistry<Id, *>.getAllHierarchical(clazz: Class<Clazz>): Map<Id, Type> {
    val rawMap = this.getAll()
    return buildMap {
        for ((clazz, reg) in rawMap) {
            if (clazz.isAssignableFrom(clazz)) {
                putAll(reg.getAll() as Map<Id, Type>)
            }
        }
    }
}

@Suppress("UNCHECKED_CAST")
inline fun <Id, reified Clazz, reified Type> MutableRegistry<Class<*>, FrozenRegistry<Id, Type>>.register(
    id: Id,
    value: Type
) {
    val reg = this[Clazz::class.java]?.unfreeze() ?: MutableRegistry()
    reg.register(id, value)
    this.register(Clazz::class.java, reg.freeze())
}


@Suppress("UNCHECKED_CAST")
inline fun <Id, reified Clazz, reified Type> MutableRegistry<Class<*>, FrozenRegistry<Id, Type>>.register(
    map: Map<Id, Type>
) {
    val reg = this[Clazz::class.java]?.unfreeze() ?: MutableRegistry()
    map.forEach { (id, value) -> reg.register(id, value) }
    this.register(Clazz::class.java, reg.freeze())
}