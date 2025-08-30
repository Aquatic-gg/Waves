package gg.aquatic.waves.scenario.action.path

import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.prop.Moveable
import gg.aquatic.waves.scenario.prop.path.PathBoundProperties
import gg.aquatic.waves.scenario.prop.path.PathProp
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import java.util.concurrent.ConcurrentHashMap

@RegisterAction("bind-path")
class BindPathAction : Action<Scenario>{
    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("object-id", "model", true),
        BoundPathObjectArgument(
            "bound-paths",
            { _ -> ConcurrentHashMap() },
            false
        )
    )

    @Suppress("UNCHECKED_CAST")
    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        val objectId = args.string("object-id") { textUpdater(binder, it) } ?: return
        val key = Key.key(objectId)

        val boundPropertiesFactory = args.any("bound-paths") as ((Scenario) -> ConcurrentHashMap<PathProp, PathBoundProperties>)? ?: { _ -> ConcurrentHashMap() }

        val prop = binder.props[key] as? Moveable ?: return
        var i = 0
        val boundPaths = boundPropertiesFactory(binder)


        prop.boundPaths += boundPaths.mapValues {
            i++
            it.value to prop.boundPaths.size + i
        }
        for ((path, pathProperties) in boundPaths) {
            path.boundProps += prop to pathProperties
        }
    }
}