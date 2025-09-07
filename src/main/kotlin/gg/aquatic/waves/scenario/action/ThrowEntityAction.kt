package gg.aquatic.waves.scenario.action

import gg.aquatic.waves.scenario.prop.Throwable
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

@RegisterAction("throw-entity")
class ThrowEntityAction : Action<Scenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("velocity", null, false),
        PrimitiveObjectArgument("pitch", 0.0, false),
        PrimitiveObjectArgument("yaw", 0.0, false),
        PrimitiveObjectArgument("power", 1.0, false),
        PrimitiveObjectArgument("prop", "entity:example", true)
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        var velocity = args.vector("velocity") { textUpdater(binder, it) }
        val power = args.double("power") { textUpdater(binder, it) } ?: 1.0
        val property = args.string("prop") { textUpdater(binder, it) } ?: "entity:example"

        if (velocity == null) {
            val yaw = args.double("yaw") { textUpdater(binder, it) } ?: 0.0
            val pitch = args.double("pitch") { textUpdater(binder, it) } ?: 0.0

            velocity = vectorFromYawPitch(yaw.toFloat(), pitch.toFloat())
        }

        val prop = binder.props[Key.key(property)] ?: return
        if (prop !is Throwable) return

        prop.throwObject(velocity.clone().multiply(power))
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