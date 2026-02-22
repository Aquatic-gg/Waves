package gg.aquatic.clientside.serialize

import gg.aquatic.blokk.Blokk
import gg.aquatic.blokk.BlokkSerializer
import gg.aquatic.clientside.ObjectInteractEvent
import gg.aquatic.clientside.block.FakeBlock
import gg.aquatic.common.audience.AquaticAudience
import org.bukkit.Location
import org.bukkit.configuration.ConfigurationSection

class ClientsideBlockSettings(
    val block: Blokk,
    val viewRange: Int,
) : ClientsideSettings<FakeBlock> {
    override fun create(
        location: Location,
        audience: AquaticAudience,
        onInteract: ObjectInteractEvent<FakeBlock>
    ): FakeBlock {
        return FakeBlock(block, location, viewRange, audience, onInteract)
    }

    companion object: ClientsideSettings.Factory {
        override fun fromSection(section: ConfigurationSection): ClientsideBlockSettings {
            val block = BlokkSerializer.load(section)
            val viewRange = section.getInt("view-range", 50)
            return ClientsideBlockSettings(block, viewRange)
        }
    }
}