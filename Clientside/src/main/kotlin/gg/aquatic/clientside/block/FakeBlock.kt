package gg.aquatic.clientside.block

import gg.aquatic.blokk.Blokk
import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.FakeObjectHandler
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import gg.aquatic.pakket.Pakket
import gg.aquatic.pakket.api.nms.toBlockPos
import gg.aquatic.pakket.isChunkTracked
import gg.aquatic.pakket.sendPacket
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class FakeBlock(
    block: Blokk,
    location: Location,
    viewRange: Int,
    audience: AquaticAudience,
    var onInteract: ObjectInteractEvent<FakeBlock> = { _, _, _ -> },
    var onTick: suspend () -> Unit = {}
) : FakeObject(viewRange, audience) {

    override val location: Location = location.toBlockLocation()
    private val dataLocation = location.toBlockLocation().apply {
        yaw = location.yaw
        pitch = location.pitch
    }
    var block: Blokk = block
        private set

    init {
        setAudience(audience)
    }

    @Suppress("unused")
    fun changeBlock(aquaticBlock: Blokk) {
        this.block = aquaticBlock
        isViewing.forEach { onShow(it) }
    }

    internal fun injectViewer(player: Player) {
        this._isViewing += player.uniqueId
    }
    internal fun ejectViewer(player: Player) {
        this._isViewing -= player.uniqueId
    }

    internal fun renderedBlockData() = block.blockDataAt(dataLocation)

    override fun onShow(player: Player) {
        val packet = Pakket.handler.createBlockChangePacket(location, renderedBlockData())
        player.sendPacket(packet, true)
    }

    override fun onHide(player: Player) {
        val packet = Pakket.handler.createBlockChangePacket(location, location.block.blockData)
        player.sendPacket(packet, false)
    }

    override fun handleInteract(player: Player, isLeftClick: Boolean) {
        onInteract.onInteract(this, player, isLeftClick)
        // Anti-ghosting correction for right-clicks
        if (!isLeftClick && !destroyed) {
            onShow(player)
        }
    }

    override fun register() {
        if (registered) return
        registered = true

        FakeObjectHandler.tickableObjects += this

        val chunkX = Math.floorDiv(location.blockX, 16)
        val chunkZ = Math.floorDiv(location.blockZ, 16)
        val bundle = FakeObjectHandler.getOrCreateChunkCacheBundle(chunkX, chunkZ, location.world)
        val set = bundle.blocks.computeIfAbsent(location.toBlockPos()) { ConcurrentHashMap.newKeySet() }
        set += this
    }

    fun unregister() {
        if (!registered) return
        registered = false
        val chunkX = Math.floorDiv(location.blockX, 16)
        val chunkZ = Math.floorDiv(location.blockZ, 16)
        val bundle = FakeObjectHandler.getChunkCacheBundle(chunkX, chunkZ, location.world) ?: return
        bundle.blocks[location.toBlockPos()]?.remove(this)
    }

    override fun destroy() {
        if (!markDestroyed()) return
        val correctionTargets = buildSet {
            addAll(viewers)
            addAll(isViewing)
        }.filter { player ->
            player.world.name == location.world?.name && player.isChunkTracked(location.chunk)
        }

        _isViewing.clear()
        _viewers.clear()

        correctionTargets.forEach(::onHide)

        FakeObjectHandler.tickableObjects -= this
        unregister()
    }

    override suspend fun tick() {
        onTick()
    }
}
