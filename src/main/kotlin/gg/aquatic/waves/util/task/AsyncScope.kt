package gg.aquatic.waves.util.task

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

object AsyncScope: CoroutineScope {
    override val coroutineContext: CoroutineContext = AsyncCtx
}