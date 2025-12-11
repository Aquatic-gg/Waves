package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription

fun interface EventExceptionHandler {

    fun handleException(subscription: Subscription<*>, event: Any, throwable: Throwable)

    object PRINT_STACKTRACE: EventExceptionHandler {
        override fun handleException(
            subscription: Subscription<*>,
            event: Any,
            throwable: Throwable
        ) {
            throwable.printStackTrace()
        }

    }
}