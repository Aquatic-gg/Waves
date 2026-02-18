package gg.aquatic.kregistry.core

import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistryKey

interface RegistryKey<K, V> {
    val id: RegistryId

    companion object {
        fun <K, V> simple(id: RegistryId): SimpleRegistryKey<K, V> = SimpleRegistryKey(id)

        fun <Id, Group, Value : GroupedEntry<Group>> grouped(
            id: RegistryId
        ): GroupedRegistryKey<Id, Group, Value> = GroupedRegistryKey(id)
    }
}

data class SimpleRegistryKey<K, V>(
    override val id: RegistryId
) : RegistryKey<K, V>
