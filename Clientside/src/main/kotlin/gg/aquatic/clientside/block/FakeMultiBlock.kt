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
    override val viewRange: Int,
    initialAudience: AquaticAudience,
    var onInteract: ObjectInteractEvent<FakeMultiBlock> = {},
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

    fun register() {
        blocks.forEach { it.register() }
    }

    fun unregister() {
        blocks.forEach { it.unregister() }
    }

    override fun addViewer(player: Player) {
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
        destroyed = true
        blocks.forEach { it.destroy() }
        blocks.clear()
    }
}
