package gg.aquatic.clientside.bm

import gg.aquatic.clientside.FakeObject
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.entity.FakeEntity
import gg.aquatic.common.audience.AquaticAudience
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.animation.AnimationModifier
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.bukkit.platform.BukkitPlayer
import kr.toxicity.model.api.event.hitbox.HitBoxInteractEvent
import kr.toxicity.model.api.nms.ModelInteractionHand
import kr.toxicity.model.api.tracker.TrackerModifier
import kr.toxicity.model.api.tracker.TrackerUpdateAction
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.entity.Player


class FakeBM(
    override val location: Location,
    val modelId: String,
    override val viewRange: Int,
    initialAudience: AquaticAudience,
    var onInteract: ObjectInteractEvent<FakeBM> = { _, _, _ -> },
) : FakeObject(viewRange, initialAudience) {

    val model = BetterModel.model(modelId).get().create(BukkitAdapter.adapt(location), TrackerModifier.DEFAULT).apply {
        this.pipeline.viewFilter { player ->
            audience.canBeApplied((player as BukkitPlayer).source())
        }
    }

    override fun register() {
        model.listenHitBox(HitBoxInteractEvent::class.java) { e ->
            val isLeft = e.hand == ModelInteractionHand.LEFT

            val player = e.who as BukkitPlayer
            handleInteract(player.source(), isLeft)
        }
    }

    override fun onShow(player: Player) {}
    override fun onHide(player: Player) {}

    override fun handleInteract(player: Player, isLeftClick: Boolean) {
        onInteract.onInteract(this, player, isLeftClick)
    }

    @Suppress("unused")
    fun setTint(tint: Color) {
        model.update(TrackerUpdateAction.tint(tint.asRGB()))
    }

    @Suppress("unused")
    fun playAnimation(id: String, lerpIn: Double = 0.0, lerpOut: Double = 0.0, speed: Double = 1.0) {
        model.animate(id, AnimationModifier.builder().start(lerpIn.toInt()).end(lerpOut.toInt()).build())
    }

    override fun destroy() {
        destroyed = true
        model.close()
        _viewers.clear()
    }
}