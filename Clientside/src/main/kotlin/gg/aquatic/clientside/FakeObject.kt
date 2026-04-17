package gg.aquatic.clientside

import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.common.coroutine.VirtualsCtx
import gg.aquatic.pakket.isChunkTracked
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean

abstract class FakeObject(
    val viewRange: Int,
    initialAudience: AquaticAudience
) {

    abstract val location: Location

    private val destroyedState = AtomicBoolean(false)
    val destroyed: Boolean
        get() = destroyedState.get()

    var registered: Boolean = false
        protected set

    abstract fun register()

    private val viewRangeSquared = viewRange * viewRange

    private var _audience: AquaticAudience = initialAudience
    open val audience: AquaticAudience get() = _audience

    // List of players that can see the object
    protected val _viewers: MutableSet<UUID> = ConcurrentHashMap.newKeySet<UUID>()
    val viewers: Set<Player> get() = buildSet {
        for (uUID in _viewers) {
            add(Bukkit.getPlayer(uUID) ?: continue)
        }
    }

    // List of players that are currently viewing the object
    protected val _isViewing: MutableSet<UUID> = ConcurrentHashMap.newKeySet<UUID>()
    val isViewing: Set<Player> get() = buildSet {
        for (uUID in _isViewing) {
            add(Bukkit.getPlayer(uUID) ?: continue)
        }
    }

    fun isAudienceMember(player: Player): Boolean = _viewers.contains(player.uniqueId)
    fun isPacketViewer(player: Player): Boolean = _isViewing.contains(player.uniqueId)

    fun setAudience(newAudience: AquaticAudience) {
        this._audience = newAudience

        VirtualsCtx.launch {
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
    }


    open suspend fun addViewer(player: Player) {
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

    suspend fun updateVisibility(player: Player) {
        if (shouldSee(player)) {
            show(player)
        } else {
            hide(player)
        }
    }

    suspend fun shouldSee(player: Player): Boolean {
        if (destroyed || !player.isOnline) return false
        val objectLocation = location
        val objectWorld = objectLocation.world ?: return false
        if (player.world.name != objectWorld.name) return false
        if (!audience.canBeApplied(player)) return false

        val chunkX = Math.floorDiv(objectLocation.blockX, 16)
        val chunkZ = Math.floorDiv(objectLocation.blockZ, 16)
        if (!player.isChunkTracked(objectWorld, chunkX, chunkZ)) return false

        val distSq = player.location.distanceSquared(objectLocation)
        return distSq <= viewRangeSquared.toDouble()
    }

    abstract fun handleInteract(player: Player, isLeftClick: Boolean)

    open suspend fun tick() {}
    abstract fun destroy()
    protected fun markDestroyed(): Boolean = destroyedState.compareAndSet(false, true)

    private val myCycleSlot = this.hashCode().let { if (it < 0) -it else it } % 4
    internal suspend fun handleTick(tickCount: Int) {
        if (destroyed) return
        tick()

        if (tickCount == myCycleSlot) {
            refreshVisibility()
        }
    }

    private suspend fun refreshVisibility() {
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
