package gg.aquatic.dispatch

data class SchedulerMetrics(
    val totalTasks: Int,
    val activeTasks: Int,
    val pausedTasks: Int,
    val executions: Long,
    val failures: Long
)