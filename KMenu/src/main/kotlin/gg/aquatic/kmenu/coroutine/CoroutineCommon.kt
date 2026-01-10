package gg.aquatic.kmenu.coroutine

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

val KMenuCtx = SingleThreadedContext("Cache-Common")

fun <T> CompletableDeferred<T>.complete(block: () -> T): CompletableDeferred<T> = this.apply { complete(block()) }

inline fun <T> completableDeferred(block: () -> T): CompletableDeferred<T> =
    CompletableDeferred<T>().apply { complete(block()) }

inline fun CoroutineScope.runLater(delayMillis: Long, crossinline block: suspend CoroutineScope.() -> Unit) = launch {
    delay(delayMillis)
    block()
}

inline fun CoroutineScope.runTimer(
    delayMillis: Long,
    periodMillis: Long,
    crossinline block: suspend CoroutineScope.() -> Unit
) = launch {
    delay(delayMillis)
    while (isActive) {
        block()
        delay(periodMillis)
    }
}