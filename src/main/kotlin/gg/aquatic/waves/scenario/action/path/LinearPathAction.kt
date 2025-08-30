package gg.aquatic.waves.scenario.action.path

import gg.aquatic.waves.scenario.prop.path.LinearPathProp
import gg.aquatic.waves.scenario.prop.path.PathPoint
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import java.util.*

@RegisterAction("linear-path")
class LinearPathAction : Action<Scenario> {
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "linear-path1", true),
        PathPointsArgument("points", TreeMap<Int, PathPoint>(), true),
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val points = args.typed<TreeMap<Int, PathPoint>>("points") ?: return

        val path = LinearPathProp(
            points, binder
        )
        binder.props[Key.key("path:$id")] = path
    }
}