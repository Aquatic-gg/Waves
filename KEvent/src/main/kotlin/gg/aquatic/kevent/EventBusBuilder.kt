package gg.aquatic.kevent

import gg.aquatic.kevent.base.EventBusImpl
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class EventBusBuilder {

    var scope: CoroutineScope? = null
    var exceptionHandler: EventExceptionHandler = EventExceptionHandler.PRINT_STACKTRACE
    var hierarchical: Boolean = true

    fun build(): EventBus = EventBusImpl(exceptionHandler, scope ?: CoroutineScope(Dispatchers.Default), hierarchical)

}

fun eventBusBuilder(builder: EventBusBuilder.() -> Unit) = EventBusBuilder().apply(builder).build()