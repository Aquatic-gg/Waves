package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.PlayerScenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

@RegisterAction("push-player")
class PushPlayerAction : Action<PlayerScenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("velocity", null, false),
        PrimitiveObjectArgument("pitch", 0.0, false),
        PrimitiveObjectArgument("yaw", 0.0, false),
        PrimitiveObjectArgument("power", "double", true)
    )

    override fun execute(
        binder: PlayerScenario,
        args: ObjectArguments,
        textUpdater: (PlayerScenario, String) -> String
    ) {
        val power = args.double("power") { textUpdater(binder, it) } ?: 1.0
        val velocity = args.vector("velocity") { textUpdater(binder, it) }

        val velocityFinal = if (velocity == null) {
            val yaw = args.double("yaw") { textUpdater(binder, it) }
            val pitch = args.double("pitch") { textUpdater(binder, it) }

            if (yaw != null && pitch != null) {
                vectorFromYawPitch(yaw.toFloat(), pitch.toFloat())
            } else {
                null
            }
        } else velocity
        val vector = velocityFinal?.multiply(power) ?: binder.player.location.clone()
            .subtract(binder.baseLocation).toVector().normalize()
            .multiply(power)

        binder.player.velocity = vector
    }

    fun vectorFromYawPitch(yaw: Float, pitch: Float): Vector {
        val pitchRadians = Math.toRadians(pitch.toDouble())
        val yawRadians = Math.toRadians(yaw.toDouble())

        // Calculate the components
        val x = -sin(yawRadians) * cos(pitchRadians)
        val y = -sin(pitchRadians)
        val z = cos(yawRadians) * cos(pitchRadians)

        return Vector(x, y, z)
    }
}