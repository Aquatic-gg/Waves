package gg.aquatic.dispatch

import java.util.*

typealias TaskId = UUID

data class Task(
    val id: TaskId,
    val type: ScheduleType,
    val intervalMs: Long,
    val initialDelayMs: Long = intervalMs,
    val maxRepeats: Int? = null,
    val action: suspend () -> Unit
)