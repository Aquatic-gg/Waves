package gg.aquatic.clientside

import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.pakket.isChunkTracked
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap

abstract class FakeObject(
    open val viewRange: Int,
    initialAudience: AquaticAudience
) {

    abstract val location: Location

    var destroyed: Boolean = false
        protected set

    var registered: Boolean = false
        protected set

    private var _audience: AquaticAudience = initialAudience
    open val audience: AquaticAudience get() = _audience

    // List of players that can see the object
    protected val _viewers = ConcurrentHashMap.newKeySet<UUID>()
    val viewers: Set<Player> get() = _viewers.mapNotNull { Bukkit.getPlayer(it) }.toSet()

    // List of players that are currently viewing the object
    protected val _isViewing = ConcurrentHashMap.newKeySet<UUID>()
    val isViewing: Set<Player> get() = _isViewing.mapNotNull { Bukkit.getPlayer(it) }.toSet()

    fun isAudienceMember(player: Player): Boolean = _viewers.contains(player.uniqueId)
    fun isPacketViewer(player: Player): Boolean = _isViewing.contains(player.uniqueId)

    fun setAudience(newAudience: AquaticAudience) {
        this._audience = newAudience

        // Remove those no longer in audience
        val currentViewers = _viewers
        for (uuid in currentViewers) {
            val player = Bukkit.getPlayer(uuid) ?: continue
            if (!newAudience.canBeApplied(player)) {
                removeViewer(player)
            }
        }

        // Add everyone online who matches the new audience
        for (player in Bukkit.getOnlinePlayers()) {
            if (newAudience.canBeApplied(player)) {
                addViewer(player)
            }
        }
    }


    open fun addViewer(player: Player) {
        if (!_viewers.add(player.uniqueId)) return
        updateVisibility(player)
    }

    open fun removeViewer(player: Player) {
        if (_isViewing.contains(player.uniqueId)) {
            hide(player)
        }
        _viewers.remove(player.uniqueId)
    }

    fun show(player: Player) {
        if (_isViewing.add(player.uniqueId)) {
            onShow(player)
        }
    }

    fun hide(player: Player) {
        if (_isViewing.remove(player.uniqueId)) {
            onHide(player)
        }
    }

    protected abstract fun onShow(player: Player)
    protected abstract fun onHide(player: Player)

    fun updateVisibility(player: Player) {
        if (shouldSee(player)) {
            if (!_isViewing.contains(player.uniqueId)) show(player)
        } else {
            if (_isViewing.contains(player.uniqueId)) hide(player)
        }
    }

    fun shouldSee(player: Player): Boolean {
        if (destroyed || !player.isOnline) return false
        if (player.world != location.world) return false
        if (!audience.canBeApplied(player)) return false

        val distSq = player.location.distanceSquared(location)
        if (distSq > viewRange * viewRange) return false

        return player.isChunkTracked(location.chunk)
    }

    abstract fun handleInteract(player: Player, isLeftClick: Boolean)

    open suspend fun tick() {}
    abstract fun destroy()

    internal suspend fun handleTick(tickCount: Int) {
        if (destroyed) return
        tick()

        // Spread distance checks across different ticks
        // Only check visibility every 4 ticks based on object's hash to stagger load
        val myCycleSlot = this.hashCode().let { if (it < 0) -it else it } % 4

        if (tickCount == myCycleSlot) {
            refreshVisibility()
        }
    }

    private fun refreshVisibility() {
        val worldPlayers = location.world?.players ?: return

        for (player in worldPlayers) {
            // If they are in the audience, check distance/chunk
            if (audience.canBeApplied(player)) {
                _viewers.add(player.uniqueId)
                updateVisibility(player)
            } else {
                // If they were seeing it but are no longer in audience, remove
                if (_viewers.contains(player.uniqueId)) {
                    removeViewer(player)
                }
            }
        }
    }
}