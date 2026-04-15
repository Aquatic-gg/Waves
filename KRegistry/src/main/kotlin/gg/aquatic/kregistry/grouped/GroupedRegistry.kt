package gg.aquatic.kregistry.grouped

import gg.aquatic.kregistry.bootstrap.RegistryHolder
import gg.aquatic.kregistry.core.Registry

class GroupedRegistry<Id, Group, Value : GroupedEntry<Group>>(
    override val registryKey: GroupedRegistryKey<Id, Group, Value>,
    override val data: Map<Class<out Group>, Registry<Id, Value>>,
    override val holderData: Map<RegistryHolder, Map<Class<out Group>, Registry<Id, Value>>>
) : Registry<Class<out Group>, Registry<Id, Value>> {

    @Suppress("UNCHECKED_CAST")
    fun getTypedByClass(id: Id, clazz: Class<out Group>): Value? {
        val reg = data[clazz] as? Registry<Id, *> ?: return null
        return reg.get(id) as? Value
    }

    inline fun <reified G : Group> getTyped(id: Id): Value? = getTypedByClass(id, G::class.java)

    fun getHierarchicalByClass(id: Id, clazz: Class<out Group>): Value? {
        val typed = getTypedByClass(id, clazz)
        if (typed != null) return typed
        return getAllHierarchicalByClass(clazz)[id]
    }

    inline fun <reified G : Group> getHierarchical(id: Id): Value? = getHierarchicalByClass(id, G::class.java)

    @Suppress("UNCHECKED_CAST")
    fun getAllHierarchicalByClass(clazz: Class<out Group>): Map<Id, Value> {
        return buildMap {
            for ((entryClass, reg) in data) {
                if (entryClass.isAssignableFrom(clazz)) {
                    putAll(reg.all())
                }
            }
        }
    }

    inline fun <reified G : Group> getAllHierarchical(): Map<Id, Value> =
        getAllHierarchicalByClass(G::class.java)

    override fun withData(
        data: Map<Class<out Group>, Registry<Id, Value>>,
        holderData: Map<RegistryHolder, Map<Class<out Group>, Registry<Id, Value>>>
    ): Registry<Class<out Group>, Registry<Id, Value>> = GroupedRegistry(registryKey, data, holderData)
}
