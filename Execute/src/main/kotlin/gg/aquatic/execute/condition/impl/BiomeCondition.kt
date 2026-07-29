package gg.aquatic.execute.condition.impl

import gg.aquatic.common.argument.ArgumentContext
import gg.aquatic.common.argument.ObjectArgument
import gg.aquatic.common.argument.impl.PrimitiveObjectArgument
import gg.aquatic.common.coroutine.BukkitCtx
import gg.aquatic.execute.condition.type.PlayerCondition
import io.papermc.paper.registry.RegistryAccess
import io.papermc.paper.registry.RegistryKey
import kotlinx.coroutines.withContext
import org.bukkit.NamespacedKey
import org.bukkit.block.Biome
import org.bukkit.entity.Player
import java.util.Locale

object BiomeCondition : PlayerCondition() {
    override suspend fun execute(binder: Player, args: ArgumentContext<Player>): Boolean =
        withContext(BukkitCtx.ofLocation(binder.location)) {
            val allowedBiomes = args.stringOrCollection("biomes")
                ?.mapNotNull(::resolveBiome)
                ?.toSet()
                ?: return@withContext false
            return@withContext binder.location.block.biome in allowedBiomes
        }

    override val arguments: List<ObjectArgument<*>> = listOf(
        PrimitiveObjectArgument("biomes", "plains", true),
    )

    private fun resolveBiome(raw: String): Biome? {
        val normalized = raw.trim()
            .lowercase(Locale.ROOT)
            .replace(' ', '_')
        if (normalized.isEmpty()) {
            return null
        }

        val key = NamespacedKey.fromString(normalized)
            ?: NamespacedKey.minecraft(normalized)
        return RegistryAccess.registryAccess().getRegistry(RegistryKey.BIOME).get(key)
    }
}
