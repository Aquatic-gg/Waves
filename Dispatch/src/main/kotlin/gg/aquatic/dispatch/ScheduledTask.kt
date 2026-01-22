package gg.aquatic.dispatch

class ScheduledTask(private val scheduler: CoroutineScheduler, val id: TaskId) {
    fun pause() = scheduler.pause(id)
    fun resume() = scheduler.resume(id)
    fun cancel() = scheduler.cancel(id)
}