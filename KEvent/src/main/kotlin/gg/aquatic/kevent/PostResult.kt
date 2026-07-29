package gg.aquatic.kevent

interface PostResult<T, S> {

    val event: T

    fun getSuccessfulCalls(): Int

    fun getFailedCalls(): Int

    fun getExecutionTimes(): Map<S, Long>

}
