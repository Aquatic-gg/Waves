package gg.aquatic.kevent

import gg.aquatic.kevent.subscription.Subscription

interface PostResult<T> {

    val event: T

    fun getSuccessfulCalls(): Int

    fun getFailedCalls(): Int

    fun getExecutionTimes(): Map<Subscription<T>, Long>

}