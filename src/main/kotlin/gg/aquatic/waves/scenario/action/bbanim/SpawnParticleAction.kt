package gg.aquatic.waves.scenario.action.bbanim

import com.destroystokyo.paper.ParticleBuilder
import gg.aquatic.waves.item.AquaticItem
import gg.aquatic.waves.scenario.prop.bbanim.BBAnimProp
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.ItemObjectArgument
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import gg.aquatic.waves.util.item.toCustomItem
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.util.Vector

@RegisterAction("particle")
class SpawnParticleAction: Action<BBAnimProp.BonePart> {

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("particle", "example", true),
        PrimitiveObjectArgument("block-material", "stone", false),
        PrimitiveObjectArgument("dust-scale", 1f, false),
        PrimitiveObjectArgument("color", "0;0;0", false),
        PrimitiveObjectArgument("start-color", "0;0;0", false),
        PrimitiveObjectArgument("end-color", "0;0;0", false),
        PrimitiveObjectArgument("sculk-roll", 0f, false),
        PrimitiveObjectArgument("delay", 1, false),
        PrimitiveObjectArgument("vector", "0;0;0", false),
        PrimitiveObjectArgument("duration", 1, false),
        PrimitiveObjectArgument("location-offset", null, false),
        PrimitiveObjectArgument("offset", "0;0;0", false),
        PrimitiveObjectArgument("speed", 0f, false),
        PrimitiveObjectArgument("count", 1, false),
        ItemObjectArgument("item", Material.STONE.toCustomItem(), false)
    )

    @Suppress("UNCHECKED_CAST")
    override fun execute(binder: BBAnimProp.BonePart, args: ObjectArguments, textUpdater: (BBAnimProp.BonePart, String) -> String) {
        val particleId = args.string("particle") { textUpdater(binder, it) } ?: return

        val particleType = Particle.valueOf(particleId.uppercase())
        val particleBuilder = when (particleType) {
            Particle.BLOCK, Particle.BLOCK_MARKER, Particle.FALLING_DUST, Particle.DUST_PLUME, Particle.BLOCK_CRUMBLE -> {
                val blockMaterial =
                    Material.valueOf((args.string("block-material") { textUpdater(binder, it) } ?: "stone").uppercase())
                ParticleBuilder(particleType).data(blockMaterial).spawn()
                    .data(blockMaterial.createBlockData())
            }

            Particle.DUST -> {
                val scale = args.float("dust-scale") { textUpdater(binder, it) } ?: 1f
                val color = args.string("color") { textUpdater(binder, it) } ?: "0;0;0"
                val rgb = color.split(";").map { it.toIntOrNull() ?: 0 }

                particleType.builder()
                    .color(Color.fromRGB(rgb[0], rgb[1], rgb[2]), scale)
            }

            Particle.DUST_COLOR_TRANSITION -> {
                val scale = args.float("dust-scale") { textUpdater(binder, it) } ?: 1f
                val startColor = args.string("start-color") { textUpdater(binder, it) } ?: "0;0;0"
                val endColor = args.string("end-color") { textUpdater(binder, it) } ?: "0;0;0"
                val rgb1 = startColor.split(";").map { it.toIntOrNull() ?: 0 }
                val rgb2 = endColor.split(";").map { it.toIntOrNull() ?: 0 }

                particleType.builder().colorTransition(
                    Color.fromRGB(rgb1[0], rgb1[1], rgb1[2]),
                    Color.fromRGB(rgb2[0], rgb2[1], rgb2[2]),
                    scale
                )
            }

            Particle.ENTITY_EFFECT -> {
                val color = args.string("color") { textUpdater(binder, it) } ?: "0;0;0;0"
                val rgb = color.split(";").map { it.toIntOrNull() ?: 0 }

                particleType.builder().color(Color.fromRGB(rgb[0], rgb[1], rgb[2]))
            }

            Particle.SCULK_CHARGE -> {
                val sculkRoll = args.float("sculk-roll") { textUpdater(binder, it) } ?: 0f
                particleType.builder().data(sculkRoll)
            }

            Particle.ITEM -> {
                val item = args.typed<AquaticItem>("item") { textUpdater(binder, it) } ?: Material.STONE.toCustomItem()
                particleType.builder().data(item.getItem())
            }

            Particle.SHRIEK -> {
                val delay = args.int("delay") { textUpdater(binder, it) } ?: 1
                particleType.builder().data(delay)
            }

            Particle.TRAIL -> {
                val duration = args.int("duration") { textUpdater(binder, it) } ?: 1
                val vector = args.string("vector") { textUpdater(binder, it) } ?: "0;0;0"
                val actualVector = vector.split(";").map { it.toDoubleOrNull() ?: 0.0 }
                val rgb = vector.split(";").map { it.toIntOrNull() ?: 0 }
                val color = Color.fromRGB(rgb[0], rgb[1], rgb[2])

                particleType.builder().data(
                    Particle.Trail(
                        binder.location.clone().add(Vector(actualVector[0], actualVector[1], actualVector[2])),
                        color,
                        duration
                    )
                )
            }

            else -> {
                particleType.builder()
            }
        }

        val locationOffsets = args.stringOrCollection("location-offset") { textUpdater(binder, it) } ?: listOf("0;0;0")
        particleBuilder.receivers(
            Bukkit.getOnlinePlayers().filter {
                if (it.location.world != binder.location.world) return@filter false
                if (it.location.distanceSquared(binder.location) > 1000) return@filter false
                if (!binder.audience.canBeApplied(it)) return@filter false
                true
            }
        )

        val offsetStr = args.string("offset") { textUpdater(binder, it) } ?: "0;0;0"
        val offset = offsetStr.split(";").map { it.toDoubleOrNull() ?: 0.0 }
        particleBuilder.offset(offset[0], offset[1], offset[2])

        val speed = args.double("speed") { textUpdater(binder, it) } ?: 1.0
        val count = args.int("count") { textUpdater(binder, it) } ?: 1
        particleBuilder.extra(speed).count(count)

        for (locationOffsetStr in locationOffsets) {
            val locationOffset = locationOffsetStr.split(";").map { it.toDoubleOrNull() ?: 0.0 }
            val location = binder.location.clone().add(Vector(locationOffset[0], locationOffset[1], locationOffset[2]))

            particleBuilder.location(location)
            particleBuilder.spawn()
        }
    }

}