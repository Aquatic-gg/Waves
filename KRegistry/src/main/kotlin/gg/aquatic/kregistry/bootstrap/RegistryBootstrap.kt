package gg.aquatic.kregistry.bootstrap

import gg.aquatic.kregistry.core.Registry
import gg.aquatic.kregistry.core.RegistryGraph
import gg.aquatic.kregistry.core.RegistryKey
import java.util.concurrent.ConcurrentHashMap
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.AtomicReference
import kotlin.concurrent.atomics.ExperimentalAtomicApi

@OptIn(ExperimentalAtomicApi::class)
internal class RegistryBootstrap {
    private val initialized = AtomicBoolean(false)
    private val definitions = ConcurrentHashMap<RegistryKey<*, *>, RegistryDefinition<*, *>>()
    private val graphRef =
        AtomicReference(
            RegistryGraph(mapOf())
        )

    internal fun rebuildRegistries(holder: RegistryHolder) {
        require(initialized.compareAndSet(expectedValue = false, newValue = true)) { "Registry bootstrap already initialized" }
        val graph = graphRef.load()

        val registries = graph.registries.toMutableMap()

        val newRegistries = ArrayList<Registry<*, *>>()
        for ((_, registry) in registries) {
            if (holder !in registry.holderData) continue
            newRegistries += rebuildRegistry(registry, holder)
        }
        val newGraph = graph.rebuild(newRegistries)
        setGraph(newGraph)
    }

    internal fun injectContribution(contribution: Contribution) {
        require(!initialized.load()) { "Registry bootstrap already initialized" }
        for ((key, builder) in contribution.builder) {
            val definition = definitions.getOrPut(key) { RegistryDefinition(key) }
            definition.builders[contribution.holder] = builder
        }
    }

    internal fun <A, B> refreshRegistry(registry: Registry<A, B>, holder: RegistryHolder) {
        val registry = rebuildRegistry(registry, holder)
        val current = graphRef.load()
        val graphSnapshot = current.rebuild(registry)
        setGraph(graphSnapshot)
    }

    internal fun buildRegistries() {
        val registries = definitions.map { (_, definition) -> definition.build() }.associateBy { it.registryKey }
        setGraph(RegistryGraph(registries))
    }

    internal fun graph(): RegistryGraph = graphRef.load()

    internal fun <K, V> getRegistry(key: RegistryKey<K, V>): Registry<K, V> {
        return graph().getRegistry(key)
    }

    private fun setGraph(graph: RegistryGraph) {
        while (true) {
            val current = graphRef.load()
            if (graphRef.compareAndSet(current, graph)) {
                break
            }
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun <A, B> rebuildRegistry(registry: Registry<A, B>, holder: RegistryHolder): Registry<A, B> {
        val data = hashMapOf<A, B>()
        for ((otherHolder, holderMap) in registry.holderData) {
            if (otherHolder == holder) continue
            for ((key, value) in holderMap) {
                val existing = data[key]
                if (existing is Registry<*, *> && value is Registry<*, *>) {
                    data[key] = mergeRegistry(existing, value) as B
                } else {
                    data[key] = value
                }
            }
        }

        val definitions = definitions[registry.registryKey] as? RegistryDefinition<A, B> ?: return registry
        val holderBuilders = definitions.builders[holder] ?: return registry

        val builder = RegistryContributionBuilder<A, B>()
        holderBuilders(builder)

        val newHolderData = builder.data
        for ((key, value) in builder.data) {
            val existing = data[key]
            if (existing is Registry<*, *> && value is Registry<*, *>) {
                data[key] = mergeRegistry(existing, value) as B
            } else {
                data[key] = value
            }
        }

        return Registry(
            registry.registryKey,
            data,
            registry.holderData + (holder to newHolderData)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun mergeRegistry(existing: Registry<*, *>, incoming: Registry<*, *>): Registry<*, *> {
        require(existing.registryKey == incoming.registryKey) {
            "Cannot merge registries with different keys: ${existing.registryKey.id} vs ${incoming.registryKey.id}"
        }

        val mergedData = HashMap<Any, Any>()
        mergedData.putAll(existing.data as Map<Any, Any>)
        mergedData.putAll(incoming.data as Map<Any, Any>)

        val mergedHolderData = HashMap<RegistryHolder, Map<Any, Any>>()
        mergedHolderData.putAll(existing.holderData as Map<RegistryHolder, Map<Any, Any>>)
        mergedHolderData.putAll(incoming.holderData as Map<RegistryHolder, Map<Any, Any>>)

        return Registry(
            existing.registryKey as RegistryKey<Any, Any>,
            mergedData,
            mergedHolderData
        )
    }
}
