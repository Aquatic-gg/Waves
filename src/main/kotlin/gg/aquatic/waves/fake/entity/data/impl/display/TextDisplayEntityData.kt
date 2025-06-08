package gg.aquatic.waves.fake.entity.data.impl.display

import gg.aquatic.waves.Waves
import gg.aquatic.waves.api.nms.entity.DataSerializerTypes
import gg.aquatic.waves.api.nms.entity.EntityDataValue
import gg.aquatic.waves.fake.entity.data.EntityData
import gg.aquatic.waves.fake.entity.data.impl.living.BaseEntityData
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.toMMComponent
import gg.aquatic.waves.util.version.ServerVersion
import net.kyori.adventure.text.Component
import org.bukkit.entity.TextDisplay
import java.awt.Color

object TextDisplayEntityData: BaseEntityData() {

    object Text: EntityData {
        override val id: String = "text"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.string(id, updater)?.toMMComponent() ?: return emptyList())
        }

        fun generate(component: Component): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            23,
                            DataSerializerTypes.COMPONENT,
                            component
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, null, false),
        )
    }
    object Width: EntityData {
        override val id: String = "line-width"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.int(id, updater) ?: return emptyList())
        }

        fun generate(width: Int): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            24,
                            DataSerializerTypes.INT,
                            width
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, null, false),
        )
    }
    object BackgroundColor: EntityData {
        override val id: String = "background-color"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.color(id, updater) ?: return emptyList())
        }

        fun generate(color: org.bukkit.Color): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            25,
                            DataSerializerTypes.INT,
                            color.asARGB()
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, null, false),
        )
    }
    object TextOpacity: EntityData {
        override val id: String = "text-opacity"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            return generate(arguments.byte(id, updater) ?: return emptyList())
        }

        fun generate(opacity: Byte): Collection<EntityDataValue> {
            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            26,
                            DataSerializerTypes.BYTE,
                            opacity
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument(id, null, false),
        )
    }
    object Flags: EntityData {
        override val id: String = "text-display-flags"

        override fun generate(arguments: ObjectArguments, updater: (String) -> String): Collection<EntityDataValue> {
            val hasShadow = arguments.boolean("$id.has-shadow", updater) ?: false
            val isSeeThrough = arguments.boolean("$id.is-see-through", updater) ?: false
            val useDefaultBackground = arguments.boolean("$id.use-default-background", updater) ?: false
            val alignment = arguments.enum<TextDisplay.TextAlignment>("$id.alignment", updater) ?: TextDisplay.TextAlignment.CENTER

            return generate(hasShadow, isSeeThrough, useDefaultBackground, alignment)
        }

        fun generate(
            hasShadow: Boolean = false,
            isSeeThrough: Boolean = false,
            useDefaultBackground: Boolean = false,
            alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
        ): Collection<EntityDataValue> {

            val byte = packFlags(
                hasShadow,
                isSeeThrough,
                useDefaultBackground,
                alignment
            )

            when (ServerVersion.ofAquatic(Waves.INSTANCE)) {
                ServerVersion.V_1_21_1 -> {}
                ServerVersion.V_1_21_4 -> {
                    return listOf(
                        EntityDataValue.create(
                            27,
                            DataSerializerTypes.BYTE,
                            byte
                        )
                    )
                }

                else -> {}
            }
            return emptyList()
        }

        private fun packFlags(
            hasShadow: Boolean = false,
            isSeeThrough: Boolean = false,
            useDefaultBackground: Boolean = false,
            alignment: TextDisplay.TextAlignment = TextDisplay.TextAlignment.CENTER,
        ): Byte {
            var result: Byte = 0

            // Set boolean flags using specific bit masks
            if (hasShadow) result = (0 or 0x01).toByte()
            if (isSeeThrough) result = (result.toInt() or 0x02).toByte()
            if (useDefaultBackground) result = (result.toInt() or 0x04).toByte()

            // Set enum ordinal in bits 3-7
            // We shift the ordinal left by 3 bits to position it correctly (starting at bit mask 0x08)
            result = (result.toInt() or (alignment.ordinal shl 3)).toByte()

            return result
        }

        override val arguments: List<AquaticObjectArgument<*>> = listOf(
            PrimitiveObjectArgument("$id.has-shadow", false, required = false),
            PrimitiveObjectArgument("$id.is-see-through", false, required = false),
            PrimitiveObjectArgument("$id.use-default-background", false, required = false),
            PrimitiveObjectArgument("$id.alignment", "CENTER", required = false),
        )
    }

}