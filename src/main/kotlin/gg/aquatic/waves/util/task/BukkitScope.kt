package gg.aquatic.waves.util.task

import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

object BukkitScope: CoroutineScope {
    override val coroutineContext: CoroutineContext = BukkitCtx
}