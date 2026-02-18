package gg.aquatic.kregistry.grouped

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.core.SimpleRegistryKey

data class GroupedRegistryKey<Id, Group, Value : GroupedEntry<Group>>(
    override val id: RegistryId
) : RegistryKey<Class<out Group>, Registry<Id, Value>> {
    val innerKey: SimpleRegistryKey<Id, Value> =
        SimpleRegistryKey(RegistryId(id.namespace, "${id.key}__group"))
}
