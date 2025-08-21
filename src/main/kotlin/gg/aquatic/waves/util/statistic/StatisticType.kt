package gg.aquatic.waves.util.statistic

import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments

abstract class StatisticType<T> {

    abstract val arguments: Collection<AquaticObjectArgument<*>>

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