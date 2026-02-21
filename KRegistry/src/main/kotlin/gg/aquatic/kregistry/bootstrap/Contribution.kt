package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.core.SimpleRegistry
import gg.aquatic.kregistry.core.SimpleRegistryKey
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistryKey

internal class Contribution(
    val holder: RegistryHolder,
    val builder: Map<RegistryKey<*, *>, RegistryContributionBuilder<*, *>.() -> Unit>
)

class ContributionBuilder {
    private val data = HashMap<RegistryKey<*, *>, RegistryContributionBuilder<*, *>.() -> Unit>()

    @Suppress("UNCHECKED_CAST")
    fun <A, B> registry(key: SimpleRegistryKey<A, B>, builder: RegistryContributionBuilder<A, B>.() -> Unit) {
        data[key] = builder as RegistryContributionBuilder<*, *>.() -> Unit
    }

    @Suppress("UNCHECKED_CAST")
    fun <Id, Group, Value : GroupedEntry<Group>> registry(
        key: GroupedRegistryKey<Id, Group, Value>,
        builder: GroupedContributionBuilder<Id, Group, Value>.() -> Unit
    ) {
        val adapter: RegistryContributionBuilder<*, *>.() -> Unit = {
            @Suppress("UNCHECKED_CAST")
            val target = this as RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>
            val groupedBuilder = GroupedContributionBuilder(target, key.innerKey)
            groupedBuilder.builder()
        }
        data[key] = adapter
    }

    internal fun build(holder: RegistryHolder): Contribution {
        return Contribution(holder, data)
    }
}

class RegistryContributionBuilder<A, B> {

    internal val data = HashMap<A, B>()

    fun add(key: A, value: B) {
        data[key] = value
    }
}

class GroupedContributionBuilder<Id, Group, Value : GroupedEntry<Group>>(
    private val target: RegistryContributionBuilder<Class<out Group>, Registry<Id, Value>>,
    private val innerKey: SimpleRegistryKey<Id, Value>
) {
    fun group(group: Class<out Group>, builder: RegistryContributionBuilder<Id, Value>.() -> Unit) {
        val innerBuilder = RegistryContributionBuilder<Id, Value>()
        innerBuilder.builder()
        val registry = SimpleRegistry(innerKey, innerBuilder.data, emptyMap())
        target.add(group, registry)
    }

    inline fun <reified G : Group> group(noinline builder: RegistryContributionBuilder<Id, Value>.() -> Unit) {
        group(G::class.java, builder)
    }

    fun add(group: Class<out Group>, id: Id, value: Value) {
        val registry = target.data[group]
        if (registry is SimpleRegistry<Id, Value>) {
            val data = registry.data.toMutableMap()
            data[id] = value
            target.data[group] = SimpleRegistry(innerKey, data, emptyMap())
        } else if (registry != null) {
            val data = registry.data.toMutableMap()
            data[id] = value
            target.data[group] = SimpleRegistry(innerKey, data, emptyMap())
        } else {
            target.add(group, SimpleRegistry(innerKey, mapOf(id to value), emptyMap()))
        }
    }

    fun add(id: Id, value: Value) {
        add(value.binder, id, value)
    }
}
