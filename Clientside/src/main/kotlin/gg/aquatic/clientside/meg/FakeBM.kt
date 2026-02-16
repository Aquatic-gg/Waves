package gg.aquatic.clientside.meg

import gg.aquatic.clientside.FakeObject
import gg.aquatic.common.audience.AquaticAudience
import kr.toxicity.model.api.BetterModel
import kr.toxicity.model.api.animation.AnimationModifier
import kr.toxicity.model.api.bukkit.platform.BukkitAdapter
import kr.toxicity.model.api.bukkit.platform.BukkitPlayer
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
) : FakeObject(viewRange, initialAudience) {

    val model = BetterModel.model(modelId).get().create(BukkitAdapter.adapt(location), TrackerModifier.DEFAULT).apply {
        this.pipeline.viewFilter { player ->
            audience.canBeApplied((player as BukkitPlayer).source())
        }
    }

    override fun onShow(player: Player) {}
    override fun onHide(player: Player) {}

    override fun handleInteract(player: Player, isLeftClick: Boolean) {}

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
