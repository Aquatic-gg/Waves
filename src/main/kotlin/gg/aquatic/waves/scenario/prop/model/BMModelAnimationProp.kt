package gg.aquatic.waves.scenario.prop.model

import gg.aquatic.waves.interactable.type.BMInteractable
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.ScenarioProp
import gg.aquatic.waves.scenario.prop.Moveable
import gg.aquatic.waves.scenario.prop.path.PathBoundProperties
import gg.aquatic.waves.scenario.prop.path.PathProp
import kr.toxicity.model.api.animation.AnimationModifier
import org.bukkit.Location
import org.bukkit.util.Vector
import java.util.concurrent.ConcurrentHashMap

class BMModelAnimationProp(
    override val scenario: Scenario,
    val model: String,
    val modelAnimation: String?,
    override val locationOffset: Vector,
    override val boundPaths: ConcurrentHashMap<PathProp, Pair<PathBoundProperties, Int>>,
    override val locationOffsetYawPitch: Pair<Float, Float>
) : ScenarioProp, Moveable {

    override val processedPaths: MutableSet<PathProp> = ConcurrentHashMap.newKeySet()
    var interactable: BMInteractable? = null
        private set

    init {
        val currentLocation = if (boundPaths.isEmpty()) scenario.baseLocation.clone().add(locationOffset).apply {
            yaw += locationOffsetYawPitch.first
            pitch += locationOffsetYawPitch.second
        }
        else {
            val point = calculatePoint()
            val newLocation = scenario.baseLocation.clone().add(point.vector).add(locationOffset)
            newLocation.yaw = point.yaw + locationOffsetYawPitch.first
            newLocation.pitch = point.pitch + locationOffsetYawPitch.second

            newLocation
        }

        interactable = BMInteractable(
            currentLocation,
            model,
            scenario.audience,
        ) {}
        if (modelAnimation != null) {
            playAnimation(modelAnimation)
        }
    }

    override fun tick() {

    }

    fun playAnimation(animation: String, fadeIn: Int = 0, fadeOut: Int = 0, speed: Float = 1.0f) {
        interactable?.tracker?.animate(animation, AnimationModifier(fadeIn,fadeOut,speed))
    }

    override fun onEnd() {
        interactable?.destroy()
    }


    override fun move(location: Location) {
        interactable?.tracker?.location(location)
    }
}