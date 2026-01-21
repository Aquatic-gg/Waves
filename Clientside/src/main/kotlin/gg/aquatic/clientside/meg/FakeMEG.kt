package gg.aquatic.clientside.meg

import com.destroystokyo.paper.profile.PlayerProfile
import com.ticxo.modelengine.api.ModelEngineAPI
import com.ticxo.modelengine.api.model.ActiveModel
import com.ticxo.modelengine.api.model.ModeledEntity
import com.ticxo.modelengine.api.model.bone.BoneBehaviorTypes
import com.ticxo.modelengine.api.model.bone.type.PlayerLimb
import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.function.Predicate

class FakeMEG(
    override val location: Location,
    val modelId: String,
    override val viewRange: Int,
    initialAudience: AquaticAudience,
    var onInteract: ObjectInteractEvent<FakeMEG> = {}
) : FakeObject(viewRange, initialAudience) {

    val dummy = MEGInteractableDummy(this).apply {
        location = this@FakeMEG.location
        val rot = this@FakeMEG.location
        bodyRotationController.yBodyRot = rot.yaw
        bodyRotationController.xHeadRot = rot.pitch
        bodyRotationController.yHeadRot = rot.yaw
        yHeadRot = rot.yaw
        yBodyRot = rot.yaw
    }

    val modeledEntity: ModeledEntity? get() = ModelEngineAPI.getModeledEntity(dummy.uuid)
    val activeModel: ActiveModel? get() = modeledEntity?.getModel(modelId)?.orElse(null)

    init {
        dummy.data.tracked.playerPredicate = Predicate { p -> _viewers.contains(p.uniqueId) }
        val me = ModelEngineAPI.createModeledEntity(dummy)
        val model = ModelEngineAPI.createActiveModel(modelId)
        me.addModel(model, true)
    }

    override fun onShow(player: Player) {}
    override fun onHide(player: Player) {}

    override fun handleInteract(player: Player, isLeftClick: Boolean) {
        onInteract.onInteract(this, player, isLeftClick)
    }

    @Suppress("unused")
    fun setSkin(profile: PlayerProfile) {
        activeModel?.bones?.values?.forEach { bone ->
            bone.getBoneBehavior(BoneBehaviorTypes.PLAYER_LIMB).ifPresent {
                (it as PlayerLimb).setTexture(profile)
            }
        }
    }

    @Suppress("unused")
    fun setTint(tint: Color) {
        activeModel?.defaultTint = tint
    }

    @Suppress("unused")
    fun playAnimation(id: String, lerpIn: Double = 0.0, lerpOut: Double = 0.0, speed: Double = 1.0) {
        activeModel?.animationHandler?.playAnimation(id, lerpIn, lerpOut, speed, true)
    }

    override fun destroy() {
        destroyed = true
        activeModel?.let {
            it.destroy()
            it.isRemoved = true
        }
        dummy.isRemoved = true
        _viewers.clear()
    }
}
