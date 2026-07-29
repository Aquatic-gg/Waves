package gg.aquatic.kevent.base

import gg.aquatic.kevent.PostResult

class BasicMeasuredPostResult<T, S>(
    override val event: T,
    private val success: Int,
    private val fail: Int,
    private val executionTimes: Map<S, Long>
) : PostResult<T, S> {
    override fun getSuccessfulCalls(): Int {
        return success
    }

    override fun getFailedCalls(): Int {
        return fail
    }

    override fun getExecutionTimes(): Map<S, Long> {
        return executionTimes
    }
}
