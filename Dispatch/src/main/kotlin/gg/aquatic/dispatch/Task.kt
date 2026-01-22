package gg.aquatic.dispatch

import java.util.UUID

typealias TaskId = UUID

data class Task(
    val id: TaskId,
    val type: ScheduleType,
    val intervalMs: Long,
    val action: suspend () -> Unit
)