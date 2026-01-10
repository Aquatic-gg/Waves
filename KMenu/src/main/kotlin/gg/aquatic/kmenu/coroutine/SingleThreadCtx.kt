package gg.aquatic.kmenu.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

abstract class SingleThreadCtx: CoroutineDispatcher() {

    abstract val scope: CoroutineScope

    abstract fun launch(block: suspend CoroutineScope.() -> Unit): Job
}