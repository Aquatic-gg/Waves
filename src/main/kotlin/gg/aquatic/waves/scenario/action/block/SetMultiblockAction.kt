package gg.aquatic.waves.scenario.action.block

import gg.aquatic.waves.scenario.prop.block.BlockAnimationProp
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.BlockArgument
import gg.aquatic.waves.util.argument.impl.VectorListArgument
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.block.impl.VanillaBlock
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import org.bukkit.Material
import org.bukkit.util.Vector

@RegisterAction("set-multiblock")
class SetMultiblockAction : Action<Scenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        //PrimitiveObjectArgument("id", "example", true),
        VectorListArgument("offsets", listOf(), false),
        BlockArgument("block", VanillaBlock(Material.STONE.createBlockData()), true)
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        //val id = args.string("id") { textUpdater(binder, it) } ?: return
        val offsets = args.typed<List<Vector>>("offsets") ?: return
        val block = args.typed<AquaticBlock>("block") ?: return

        for (offset in offsets) {
            val offsetStr = "${offset.x.toInt()}_${offset.y.toInt()}_${offset.z.toInt()}"

            val key = Key.key("block:$offsetStr")
            binder.props[key]?.onEnd()
            val prop = BlockAnimationProp(binder, block, offset)
            binder.props[key] = prop
        }
    }
}