package gg.aquatic.waves.scenario.action.hologram

import gg.aquatic.waves.hologram.AquaticHologram
import gg.aquatic.waves.hologram.HologramSerializer
import gg.aquatic.waves.scenario.Scenario
import gg.aquatic.waves.scenario.prop.HologramScenarioProp
import gg.aquatic.waves.scenario.prop.Seatable
import gg.aquatic.waves.util.action.RegisterAction
import gg.aquatic.waves.util.argument.AbstractObjectArgumentSerializer
import gg.aquatic.waves.util.argument.AquaticObjectArgument
import gg.aquatic.waves.util.argument.ObjectArguments
import gg.aquatic.waves.util.argument.impl.PrimitiveObjectArgument
import gg.aquatic.waves.util.generic.Action
import net.kyori.adventure.key.Key
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.util.Vector

@RegisterAction("show-hologram")
class ShowHologramAnimationAction : Action<Scenario> {
    override fun execute(
        binder: Scenario,
        args: ObjectArguments,
        textUpdater: (Scenario, String) -> String,
    ) {
        val id = args.string("id") { textUpdater(binder, it) } ?: return
        val hologramSettings = args.typed<AquaticHologram.Settings>("hologram") ?: return
        val locationOffsetStrings = (args.string("location-offset") { textUpdater(binder, it) } ?: "").split(";")

        val locationOffsetVector = Vector(
            locationOffsetStrings.getOrNull(0)?.toDoubleOrNull() ?: 0.0,
            locationOffsetStrings.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
            locationOffsetStrings.getOrNull(2)?.toDoubleOrNull() ?: 0.0,
        )
        val locationOffsetYawPitch =
            (locationOffsetStrings.getOrNull(3)?.toFloatOrNull() ?: 0.0f) to (locationOffsetStrings.getOrNull(4)
                ?.toFloatOrNull() ?: 0.0f)

        val hologram = hologramSettings.create(
            binder.baseLocation.clone().add(locationOffsetVector).apply {
                yaw += locationOffsetYawPitch.first
                pitch += locationOffsetYawPitch.second
            }, { p, str ->
                textUpdater(binder, str)
            },
            { p -> binder.audience.canBeApplied(p) }
        )

        val key = Key.key("hologram:$id")
        val prop = HologramScenarioProp(binder, hologram)
        binder.props[key] = prop

        val seatId = args.string("seat") { s -> textUpdater(binder, s) } ?: return
        val seat = binder.props[Key.key(seatId)] as? Seatable ?: return
        val seatEntityId = seat.entityId

        hologram.setAsPassenger(seatEntityId)
    }

    override val arguments: List<AquaticObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("id", "example", true),
        HologramArgument("hologram", null, true),
        PrimitiveObjectArgument("seat", null, false),
        PrimitiveObjectArgument("location-offset", "0;0;0", false, listOf("offset"))
    )

    class HologramArgument(
        id: String,
        defaultValue: AquaticHologram.Settings?, required: Boolean, aliases: Collection<String> = listOf(),
    ) : AquaticObjectArgument<AquaticHologram.Settings>(id, defaultValue, required, aliases) {
        override val serializer: AbstractObjectArgumentSerializer<AquaticHologram.Settings?> = Companion

        companion object : AbstractObjectArgumentSerializer<AquaticHologram.Settings?>() {
            override fun load(
                section: ConfigurationSection,
                id: String,
            ): AquaticHologram.Settings? {
                if (section.isConfigurationSection(id)) {
                    return HologramSerializer.loadHologram(section.getConfigurationSection(id)!!)
                }
                if (section.isList(id)) {
                    return HologramSerializer.loadHologram(section.getList(id)!!)
                }
                return null
            }

        }
    }
}