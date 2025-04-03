package gg.aquatic.waves.util

import com.tcoded.folialib.wrapper.task.WrappedTask
import gg.aquatic.waves.Waves
import org.bukkit.Location
import org.bukkit.entity.Entity

inline fun runSync(crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runNextTick {
        runnable()
    }
}

inline fun runAsync(crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runAsync {
        runnable()
    }
}

inline fun runSyncTimer(delay: Long, period: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runTimer(Runnable {
        runnable()
    }, delay, period)
}

inline fun runAsyncTimer(delay: Long, period: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runTimerAsync(Runnable {
        runnable()
    }, delay, period)
}

inline fun runLaterSync(delay: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runLater(Runnable {
        runnable()
    }, delay)
}

inline fun runLaterAsync(delay: Long, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runLaterAsync(Runnable {
        runnable()
    }, delay)
}

inline fun runAtLocation(location: Location, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runAtLocation(location) { _: WrappedTask ->
        runnable()
    }
}

inline fun runAtEntity(entity: Entity, crossinline runnable: () -> Unit) {
    Waves.INSTANCE.foliaLib.scheduler.runAtEntity(entity) { _: WrappedTask ->
        runnable()
    }
}