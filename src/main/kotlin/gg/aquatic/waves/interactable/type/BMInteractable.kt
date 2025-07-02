package gg.aquatic.waves.interactable.type

import gg.aquatic.waves.interactable.Interactable
import gg.aquatic.waves.interactable.InteractableHandler
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.util.audience.AquaticAudience
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.tracker.DummyTracker
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.UUID
import kotlin.jvm.optionals.getOrNull

class BMInteractable(
    override val location: Location,
    modelId: String,
    audience: AquaticAudience,
    override val onInteract: (InteractableInteractEvent) -> Unit,
) : Interactable() {

    override val viewers: MutableSet<Player> = mutableSetOf()

    override var audience: AquaticAudience = audience
        set(value) {
            field = value
            for (player in viewers.toList()) {
                if (!field.canBeApplied(player)) {
                    removeViewer(player)
                }
            }
            for (player in
            Bukkit.getOnlinePlayers().filter { !viewers.contains(it) }) {
                if (!field.canBeApplied(player)) continue
                addViewer(player)
            }
        }

    val uuid = UUID.randomUUID()
    val model = BetterModel.model(modelId).getOrNull() ?: throw IllegalArgumentException("Model $modelId not found!")
    val tracker: DummyTracker
    /*
    fun setSkin(player: Player) {
        activeModel?.apply {
            for (value in bones.values) {
                value.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent {
                    setSkin(player)
                }
            }
        }
    }
     */

    init {
        tracker = model.create(location)
        tracker.setUuid(uuid)

        InteractableHandler.bmIntreactables += this
        this.audience = audience
    }


    override fun addViewer(player: Player) {
        viewers.add(player)
        tracker.show(player)
        tracker.spawn(Bukkit.getOnlinePlayers().first())
    }

    override fun removeViewer(player: Player) {
        viewers.remove(player)
        tracker.hide(player)
    }


    override fun destroy() {
        tracker.despawn()
        InteractableHandler.bmIntreactables -= this
        viewers.clear()
    }

    override fun updateViewers() {
        location.world?.players?.forEach { player ->
            if (audience.canBeApplied(player)) {
                addViewer(player)
            }
        }
    }
}