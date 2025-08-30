package gg.aquatic.waves.scenario.action.block

import gg.aquatic.waves.scenario.prop.block.BlockAnimationProp
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.BlockArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.block.impl.VanillaBlock
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import org.bukkit.Material

@RegisterAction("set-block")
class SetBlockAction : Action<Scenario> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        //PrimitiveObjectArgument("id", "example", true),
        PrimitiveObjectArgument("offset", "0;0;0", false),
        BlockArgument("block", VanillaBlock(Material.STONE.createBlockData()), true)
    )

    override fun execute(binder: Scenario, args: ObjectArguments, textUpdater: (Scenario, String) -> String) {
        //val id = args.string("id") { textUpdater(binder, it) } ?: return
        val offset = args.vector("offset") { textUpdater(binder, it) } ?: return
        val block = args.typed<AquaticBlock>("block") ?: return

        val offsetStr = "${offset.x.toInt()}_${offset.y.toInt()}_${offset.z.toInt()}"
        val key = Key.key("block:$offsetStr")

        val previous = binder.prop<BlockAnimationProp>(key)
        if (previous != null) {
            previous.packetBlock.changeBlock(block)
            return
        }
        val prop = BlockAnimationProp(binder, block, offset)
        binder.props[key] = prop
    }
}