package gg.aquatic.waves.fake.entity.data.impl.display

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ItemObjectArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.version.ServerVersion
import org.bukkit.entity.Entity
import org.bukkit.entity.ItemDisplay
import org.bukkit.inventory.ItemStack

object ItemDisplayEntityData: DisplayEntityData() {

    abstract class Base: EntityData {
        override val entityClass: Class<out Entity> = ItemDisplay::class.java
    }

    object Item: Base() {
        override val id: String = "display-item"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.any(id, updater) as? ItemStack ?: return emptyList())
        }

        fun generate(item: ItemStack): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            23,
                            DataSerializerTypes.ITEM_STACK,
                            item
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            ItemObjectArgument(id, null, false),
        )
    }
    object ItemDisplayTransform: Base() {
        override val id: String = "item-display-transform"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.enum<ItemDisplay.ItemDisplayTransform>(id, updater) ?: return emptyList())
        }

        fun generate(display: ItemDisplay.ItemDisplayTransform): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            24,
                            DataSerializerTypes.BYTE,
                            display.ordinal.toByte()
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, "GROUND", false),
        )
    }

}