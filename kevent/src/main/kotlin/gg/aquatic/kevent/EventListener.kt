package gg.aquatic.kevent

fun interface EventListener<T> {

    @Throws(Exception::class)
    fun handle(event: T)

}
