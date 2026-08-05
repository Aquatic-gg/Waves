package gg.aquatic.clientside.block

import gg.aquatic.blokk.MultiBlokk
import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.ConcurrentHashMap

class FakeMultiBlock(
    val multiBlokk: MultiBlokk,
    override val location: Location,
    viewRange: Int,
    initialAudience: AquaticAudience,
    var onInteract: ObjectInteractEvent<FakeMultiBlock> = { _, _, _ -> },
    val onTick: suspend () -> Unit = {}
) : FakeObject(viewRange, initialAudience) {

    private val blocks = ConcurrentHashMap.newKeySet<FakeBlock>()

    override val audience: AquaticAudience
        get() = super.audience

    init {
        multiBlokk.processLayerCells(location) { char, newLoc ->
            val blokk = multiBlokk.shape.blocks[char] ?: return@processLayerCells
            blocks += FakeBlock(blokk, newLoc, viewRange, audience, { _, player, bool ->
                this.onInteract.onInteract(this, player, bool)
            }, onTick = onTick)
        }
    }

    override suspend fun register() {
        if (registered) return
        registered = true
        blocks.forEach { it.register() }
        bootstrapAudienceViewers()
    }

    fun unregister() {
        blocks.forEach { it.unregister() }
    }

    override suspend fun addViewer(player: Player) {
        super.addViewer(player)
        blocks.forEach { it.addViewer(player) }
    }

    override fun removeViewer(player: Player) {
        super.removeViewer(player)
        blocks.forEach { it.removeViewer(player) }
    }

    override fun onShow(player: Player) {}
    override fun onHide(player: Player) {}

    override fun handleInteract(player: Player, isLeftClick: Boolean) {

    }

    override fun destroy() {
        if (!markDestroyed()) return
        blocks.forEach { it.destroy() }
        blocks.clear()
    }

    companion object {
        suspend fun createRegistered(
            multiBlokk: MultiBlokk,
            location: Location,
            viewRange: Int,
            initialAudience: AquaticAudience,
            onInteract: ObjectInteractEvent<FakeMultiBlock> = { _, _, _ -> },
            onTick: suspend () -> Unit = {}
        ): FakeMultiBlock {
            return FakeMultiBlock(multiBlokk, location, viewRange, initialAudience, onInteract, onTick).also {
                it.register()
            }
        }
    }
}
