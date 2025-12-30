package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription

fun interface EventExceptionHandler {

    fun handleException(subscription: Subscription<*>, event: Any, throwable: Throwable)

    companion object {
        val PRINT_STACKTRACE = EventExceptionHandler { _, _, throwable -> throwable.printStackTrace() }
    }

}