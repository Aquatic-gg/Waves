package gg.aquatic.waves.fake.entity.data.impl.display

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.BlockArgument
import gg.aquatic.waves.util.block.AquaticBlock
import gg.aquatic.waves.util.block.impl.VanillaBlock
import gg.aquatic.waves.util.version.ServerVersion
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.entity.BlockDisplay
import org.bukkit.entity.Entity

object BlockDisplayEntityData: DisplayEntityData() {

    abstract class Base: EntityData {
        override val entityClass: Class<out Entity> = BlockDisplay::class.java
    }

    object BlockState: Base() {
        override val id: String = "block"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val block = arguments.any(id, updater) as? AquaticBlock ?: return emptyList()
            return generate(block.blockData)
        }

        fun generate(state: BlockData): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_4, ServerVersion.V_1_21_1, ServerVersion.V_1_21_5, ServerVersion.V_1_21_7, ServerVersion.V_1_21_9 -> {
                    return listOf(
                        EntityDataValue.create(
                            23,
                            DataSerializerTypes.BLOCK_STATE,
                            state
                        )
                    )
                }
                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            BlockArgument(id, VanillaBlock(Material.AIR.createBlockData()), false)
        )
    }

}