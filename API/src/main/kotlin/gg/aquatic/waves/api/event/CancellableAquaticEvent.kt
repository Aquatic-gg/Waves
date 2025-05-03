package gg.aquatic.waves.api.event

import org.bukkit.event.Cancellable

abstract class CancellableAquaticEvent(async: Boolean = false) : AquaticEvent(async), Cancellable {

    private var cancelled = false

    override fun isCancelled(): Boolean {
        return cancelled
    }

    override fun setCancelled(cancel: Boolean) {
        cancelled = cancel
    }

}