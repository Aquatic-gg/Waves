package gg.aquatic.waves.interactable.type

import com.destroystokyo.paper.profile.PlayerProfile
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.ModeledEntity
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb
import gg.aquatic.waves.interactable.Interactable
import gg.aquatic.waves.interactable.InteractableHandler
import gg.aquatic.waves.interactable.InteractableInteractEvent
import gg.aquatic.waves.interactable.MEGInteractableDummy
import gg.aquatic.waves.util.audience.AquaticAudience
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull

class MEGInteractable(
    override val location: Location,
    val modelId: String,
    audience: AquaticAudience,
    override val onInteract: (InteractableInteractEvent) -> Unit,
) : Interactable() {

    override val viewers: MutableSet<Player> = mutableSetOf()

    val dummy = MEGInteractableDummy(this).apply {
        location = this@MEGInteractable.location
        bodyRotationController.yBodyRot = location.yaw
        bodyRotationController.xHeadRot = location.pitch
        bodyRotationController.yHeadRot = location.yaw
        yHeadRot = location.yaw
        yBodyRot = location.yaw

    }

    override var audience: AquaticAudience = audience
        set(value) {
            field = value
            for (player in viewers.toList()) {
                if (!field.canBeApplied(player)) {
                    removeViewer(player)
                }
            }
            for (player in Bukkit.getOnlinePlayers()) {
                if (viewers.contains(player)) continue
                if (!field.canBeApplied(player)) continue
                addViewer(player)
            }
        }


    val modeledEntity: ModeledEntity?
        get() {
            return ModelEngineAPI.getModeledEntity(dummy.uuid)
        }
    val activeModel: ActiveModel?
        get() {
            return modeledEntity?.getModel(modelId)?.getOrNull()
        }

    fun setSkin(player: Player) {
        setSkin(player.playerProfile)
    }

    fun setSkin(playerProfile: PlayerProfile) {
        activeModel?.apply {
            for (value in bones.values) {
                value.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent {
                    (it as PlayerLimb).setTexture(playerProfile)
                }
            }
        }
    }

    fun setTint(tint: Color) {
        activeModel?.apply {
            this.defaultTint = tint
        }
    }

    init {
        this.audience = audience
        dummy.data.tracked.playerPredicate = Predicate { p -> viewers.contains(p) }

        val modeledEntity = ModelEngineAPI.createModeledEntity(dummy)
        val activeModel = ModelEngineAPI.createActiveModel(modelId)

        InteractableHandler.megInteractables += this
        modeledEntity.addModel(activeModel, true)
    }


    override fun addViewer(player: Player) {
        viewers.add(player)
        //dummy.setForceViewing(player, true)
    }

    override fun removeViewer(player: Player) {
        viewers.remove(player)
        //dummy.setForceViewing(player, false)
    }


    override fun destroy() {
        this.activeModel?.destroy()
        this.activeModel?.isRemoved = true
        dummy.isRemoved = true

        InteractableHandler.megInteractables -= this
        /*
        val chunkId = this.location.chunk.chunkId()
        val collection = InteractableHandler.megInteractables[chunkId]
        if (collection != null) {
            collection -= this

            if (collection.isEmpty()) {
                InteractableHandler.megInteractables -= chunkId
            }
        }
         */
        viewers.clear()
    }

    override fun updateViewers() {
        Bukkit.getOnlinePlayers().forEach { player ->
            if (audience.canBeApplied(player)) {
                addViewer(player)
            }
        }
    }
}