package gg.aquatic.clientside.serialize

import gg.aquatic.blokk.BlokkSerializer
import gg.aquatic.blokk.MultiBlokk
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.block.FakeMultiBlock
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class ClientsideMultiBlockSettings(
    val multiBlock: MultiBlokk,
    val viewRange: Int,
    val offsetX: Double = 0.0,
    val offsetY: Double = 0.0,
    val offsetZ: Double = 0.0,
): ClientsideSettings<FakeMultiBlock> {
    override fun create(
        location: Location,
        audience: AquaticAudience,
        onInteract: ObjectInteractEvent<FakeMultiBlock>
    ): FakeMultiBlock {
        return FakeMultiBlock(multiBlock, applyOffset(location, offsetX, offsetY, offsetZ), viewRange, audience, onInteract)
    }

    companion object: ClientsideSettings.Factory {
        override fun fromSection(section: ConfigurationSection): ClientsideMultiBlockSettings {
            val multiBlock = BlokkSerializer.loadMultiBlock(section)
            val viewRange = section.getInt("view-range", 50)
            return ClientsideMultiBlockSettings(
                multiBlock = multiBlock,
                viewRange = viewRange,
                offsetX = section.getDouble("offset.x", 0.0),
                offsetY = section.getDouble("offset.y", 0.0),
                offsetZ = section.getDouble("offset.z", 0.0)
            )
        }
    }
}
