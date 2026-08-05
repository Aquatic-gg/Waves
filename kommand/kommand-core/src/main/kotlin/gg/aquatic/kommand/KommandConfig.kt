package gg.aquatic.kommand

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

object KommandConfig {
    var commandScope = CoroutineScope(Dispatchers.Default)
}
