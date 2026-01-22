package gg.aquatic.dispatch

import kotlinx.coroutines.flow.StateFlow

class ScheduledTask(
    private val scheduler: CoroutineScheduler,
    val id: TaskId,
    val status: StateFlow<Status>
) {
    enum class Status {
        SCHEDULED,
        RUNNING,
        PAUSED,
        CANCELLED,
        FINISHED
    }

    fun pause() {
        if (status.value != Status.FINISHED && status.value != Status.CANCELLED) {
            scheduler.pause(id)
        }
    }

    fun resume() {
        if (status.value != Status.FINISHED && status.value != Status.CANCELLED) {
            scheduler.resume(id)
        }
    }

    fun cancel() {
        if (status.value != Status.FINISHED && status.value != Status.CANCELLED) {
            scheduler.cancel(id)
        }
    }

    val isFinished: Boolean get() = status.value == Status.FINISHED || status.value == Status.CANCELLED
}