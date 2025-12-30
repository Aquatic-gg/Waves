package gg.aquatic.kevent

import gg.aquatic.kevent.base.EventBusImpl
import kotlinx.coroutines.CoroutineScope

class EventBusBuilder {

    var scope: CoroutineScope? = null
    var exceptionHandler: EventExceptionHandler = EventExceptionHandler.PRINT_STACKTRACE
    var hierarchical: Boolean = true

    fun build(): EventBus = EventBusImpl(exceptionHandler, scope, hierarchical)

}

@Suppress("unused")
fun eventBusBuilder(builder: EventBusBuilder.() -> Unit) = EventBusBuilder().apply(builder).build()