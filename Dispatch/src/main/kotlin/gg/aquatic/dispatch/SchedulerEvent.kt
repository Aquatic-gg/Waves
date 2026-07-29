package gg.aquatic.dispatch

sealed class SchedulerEvent {
    data class TaskStarted(val id: TaskId) : SchedulerEvent()
    data class TaskCompleted(val id: TaskId, val durationMs: Long) : SchedulerEvent()
    data class TaskFailed(val id: TaskId, val error: Throwable) : SchedulerEvent()
}