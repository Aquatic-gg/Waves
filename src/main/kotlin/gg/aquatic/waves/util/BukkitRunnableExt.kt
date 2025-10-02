package gg.aquatic.waves.util

import gg.aquatic.waves.Waves

/*
inline fun runSync(crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTask(Waves.INSTANCE, Runnable {
        runnable()
    })
}

 */

/*
inline fun runAsync(crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTaskAsynchronously(Waves.INSTANCE, Runnable {
        runnable()
    })
}

 */

inline fun runSyncTimer(delay: Long, period: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTaskTimer(Waves.INSTANCE, Runnable {
        runnable()
    }, delay, period)
}

inline fun runAsyncTimer(delay: Long, period: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTaskTimerAsynchronously(Waves.INSTANCE, Runnable {
        runnable()
    }, delay, period)
}

inline fun runLaterSync(delay: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTaskLater(Waves.INSTANCE, Runnable {
        runnable()
    }, delay)
}

inline fun runLaterAsync(delay: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.server.scheduler.runTaskLaterAsynchronously(Waves.INSTANCE, Runnable {
        runnable()
    }, delay)
}