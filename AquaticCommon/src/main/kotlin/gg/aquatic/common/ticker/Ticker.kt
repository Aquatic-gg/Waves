package gg.aquatic.common.ticker

class Ticker(
    val onTick: suspend () -> Unit
) {

    var registered: Boolean = false
        private set

    fun register() {
        if (registered) {
            return
        }
        GlobalTicker.register(this)
        registered = true
    }

    fun unregister() {
        if (!registered) {
            return
        }
        GlobalTicker.unregister(this)
        registered = false
    }
}