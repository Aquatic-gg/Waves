package gg.aquatic.kevent

import gg.aquatic.kevent.base.EventBusImpl
import gg.aquatic.kevent.base.SuspendingEventBusImpl
import kotlinx.coroutines.CoroutineScope

class EventBusBuilder {

    var exceptionHandler: EventExceptionHandler = EventExceptionHandler.PRINT_STACKTRACE
    var hierarchical: Boolean = true

    fun build(): EventBus = EventBusImpl(exceptionHandler, hierarchical)
}

class SuspendingEventBusBuilder {

    var scope: CoroutineScope? = null
    var exceptionHandler: EventExceptionHandler = EventExceptionHandler.PRINT_STACKTRACE
    var hierarchical: Boolean = true

    fun build(): SuspendingEventBus {
        val busScope = requireNotNull(scope) { "Suspending EventBus requires a CoroutineScope." }
        return SuspendingEventBusImpl(exceptionHandler, busScope, hierarchical)
    }
}

@Suppress("unused")
fun eventBusBuilder(builder: EventBusBuilder.() -> Unit) = EventBusBuilder().apply(builder).build()

@Suppress("unused")
fun suspendingEventBusBuilder(builder: SuspendingEventBusBuilder.() -> Unit) =
    SuspendingEventBusBuilder().apply(builder).build()
