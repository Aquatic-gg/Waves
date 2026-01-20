package gg.aquatic.statistik

import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.kregistry.*

abstract class StatisticType<T> {

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
        val REGISTRY_KEY = RegistryKey<Class<*>, FrozenRegistry<String, StatisticType<*>>>(RegistryId("aquatic", "statistic_types"))
        val REGISTRY: TypedRegistry<String, StatisticType<*>>
            get() {
                return Registry[REGISTRY_KEY]
            }
    }
}

class StatisticHandle<T>(
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

class StatisticAddEvent<T>(val statistic: StatisticType<T>, val increasedAmount: Number, val binder: T)