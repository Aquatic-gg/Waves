package gg.aquatic.pakket.api.nms.entity.data.impl.display

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.argument.BlockArgument
import gg.aquatic.blokk.impl.VanillaBlock
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.ObjectArguments
import gg.aquatic.pakket.api.NMSVersion
import gg.aquatic.pakket.api.nms.entity.DataSerializerTypes
import gg.aquatic.pakket.api.nms.entity.EntityDataValue
import gg.aquatic.pakket.api.nms.entity.data.EntityData
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
            val block = arguments.any(id, updater) as? Blokk ?: return emptyList()
            return generate(block.blockData)
        }

        fun generate(state: BlockData): Collection<EntityDataValue> {
            when (NMSVersion.ofAquatic()) {
                NMSVersion.V_1_21_4, NMSVersion.V_1_21_1, NMSVersion.V_1_21_5, NMSVersion.V_1_21_7, NMSVersion.V_1_21_9 -> {
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

        override val arguments: List<ObjectArgument<*>> = listOf(
            BlockArgument(id, VanillaBlock(Material.AIR.createBlockData()), false)
        )
    }

}