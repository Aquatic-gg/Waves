package gg.aquatic.waves.scenario.action.bbanim

import gg.aquatic.waves.blockbench.BlockBenchAnimationHandler
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.prop.bbanim.BBAnimProp
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ActionsArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.collection.executeActions
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.generic.ConfiguredExecutableObject
import net.kyori.adventure.key.Key
import org.bukkit.util.Vector

@RegisterAction("show-bb-animation")
class ShowBBAnimAction : Action<Scenario> {
    override fun execute(
        binder: Scenario,
        args: ObjectArguments,
        textUpdater: (Scenario, String) -> String,
    ) {
        val id = args.string("id") ?: return

        val model = args.string("model") ?: return
        val animation = args.string("animation") ?: return
        val partActions =
            args.any("part-actions") as? Collection<ConfiguredExecutableObject<BBAnimProp.BonePart, Unit>> ?: return

        val offset = args.vector("location-offset") ?: return
        val location = binder.baseLocation.clone().add(offset)

        val spawned = BlockBenchAnimationHandler.spawn(location, model, { part ->
            val cachedLoc = part.lastCachedLocation ?: return@spawn
            val bonePart = BBAnimProp.BonePart(part.parent.template.name, cachedLoc, binder.audience)

            partActions.executeActions(bonePart) { _, str -> str }
        }, animation) ?: return

        val prop = BBAnimProp(binder, spawned)

        binder.props += Key.key("bbanim:$id") to prop
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", null, true),
        PrimitiveObjectArgument("model", null, true),
        PrimitiveObjectArgument("animation", null, true),
        PrimitiveObjectArgument("location-offset", Vector(), false),
        ActionsArgument("part-actions", listOf(), true, BBAnimProp.BonePart::class.java, listOf())
    )
}