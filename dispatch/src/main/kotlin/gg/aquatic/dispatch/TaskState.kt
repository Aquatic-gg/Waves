package gg.aquatic.dispatch

internal data class TaskState(
    val task: Task,
    var nextRunTime: Long,
    var paused: Boolean = false,
    var cancelled: Boolean = false
)