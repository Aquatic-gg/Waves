package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.SubscriptionInfo

fun interface EventExceptionHandler {

    fun handleException(subscription: SubscriptionInfo<*>, event: Any, throwable: Throwable)

    companion object {
        val PRINT_STACKTRACE = EventExceptionHandler { _, _, throwable -> throwable.printStackTrace() }
    }

}
