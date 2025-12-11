package gg.aquatic.kevent.base

import gg.aquatic.kevent.PostResult
import gg.aquatic.kevent.subscription.Subscription

class BasicMeasuredPostResult<T>(
    override val event: T,
    private val success: Int,
    private val fail: Int,
    private val executionTimes: Map<Subscription<T>, Long>
) : PostResult<T> {
    override fun getSuccessfulCalls(): Int {
        return success
    }

    override fun getFailedCalls(): Int {
        return fail
    }

    override fun getExecutionTimes(): Map<Subscription<T>, Long> {
        return executionTimes
    }
}