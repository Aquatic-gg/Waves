package gg.aquatic.kevent

fun interface SuspendingEventListener<T> {

    @Throws(Exception::class)
    suspend fun handle(event: T)
}
