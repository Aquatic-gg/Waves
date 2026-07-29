package gg.aquatic.statistik

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.kregistry.core.RegistryId
import gg.aquatic.kregistry.core.RegistryKey
import gg.aquatic.kregistry.grouped.GroupedEntry
import gg.aquatic.kregistry.grouped.GroupedRegistry

abstract class StatisticType<T: Any>: GroupedEntry<T> {

    abstract val arguments: Collection<ObjectArgument<*>>

    val handles = mutableListOf<StatisticHandle<T>>()

    abstract fun initialize()
    abstract fun terminate()

    fun registerHandle(handle: StatisticHandle<T>) {
        if (handles.isEmpty()) {
            initialize()
        }
        handles.add(handle)
        onRegister(handle)
    }

    open fun onRegister(handle: StatisticHandle<T>) {}

    fun unregisterHandle(handle: StatisticHandle<T>) {
        handles.remove(handle)
        onUnregister(handle)
        if (handles.isEmpty()) {
            terminate()
        }
    }

    open fun onUnregister(handle: StatisticHandle<T>) {}

    companion object {

        typealias StatisticTypeRegistry<T> = GroupedRegistry<String, T, StatisticType<T>>

        val REGISTRY_KEY = RegistryKey.grouped<String, Any, StatisticType<*>>(
            RegistryId(
                "aquatic",
                "statistic_types"
            )
        )

        @Suppress("UNCHECKED_CAST")
        fun <T : Any> StatisticTypeRegistry<*>.getHierarchical(id: String, clazz: Class<T>): StatisticType<T>? {
            return (this as GroupedRegistry<String, T, StatisticType<T>>).getHierarchicalByClass(id, clazz)
        }

        val REGISTRY: StatisticTypeRegistry<*>
            get() {
                return bootstrapHolder[REGISTRY_KEY]
            }
    }
}

class StatisticHandle<T: Any>(
    val statistic: StatisticType<T>,
    val args: ObjectArguments,
    val consumer: (StatisticAddEvent<T>) -> Unit
) {

    fun unregister() {
        statistic.unregisterHandle(this)
    }

    fun register() {
        statistic.registerHandle(this)
    }

}

class StatisticAddEvent<T: Any>(val statistic: StatisticType<T>, val increasedAmount: Number, val binder: T)