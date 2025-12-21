package gg.aquatic.kevent

fun interface EventListener<T> {

    @Throws(Exception::class)
    suspend fun handle(event: T)

}