package gg.aquatic.waves.pack

import gg.aquatic.waves.Waves
import net.radstevee.packed.core.asset.impl.SourceDirectoryAssetResolutionStrategy
import net.radstevee.packed.core.pack.PackFormat
import net.radstevee.packed.core.pack.ResourcePack
import net.radstevee.packed.core.pack.ResourcePackBuilder.Companion.resourcePack
import org.bukkit.Bukkit

object PackHandler {

    var pack: ResourcePack
        private set

    init {
        pack = createPack()
    }

    fun loadPack() {
        pack = createPack()
        PackGenerator.load()
    }

    private fun createPack(): ResourcePack {
        return resourcePack {
            meta {
                description = "Nametags Resource Pack"
                format = packVersion
                outputDir = Waves.INSTANCE.dataFolder.resolve("pack").apply {
                    mkdirs()
                }
            }
            assetResolutionStrategy =
                SourceDirectoryAssetResolutionStrategy(Waves.INSTANCE.dataFolder.resolve("tempassets"))
            install(Glyphs.spaces)
        }
    }

    val packVersion: PackFormat
        get() {
            val serverVersion = Bukkit.getServer().bukkitVersion.substringBefore("-")
            val format = when (serverVersion) {
                "1.21", "1.21.1" -> PackFormat.V1_21_TO_1_21_1
                "1.21.2", "1.21.3" -> PackFormat.V1_21_2
                "1.21.4" -> PackFormat.V1_21_4
                "1.20", "1.20.1" -> PackFormat.V1_20_TO_1_20_1
                "1.20.2" -> PackFormat.V1_20_2
                "1.20.3", "1.20.4" -> PackFormat.V1_20_3_TO_1_20_4
                "1.20.5", "1.20.6" -> PackFormat.V1_20_5_TO_1_20_6
                else -> PackFormat.LATEST
            }
            println("Using pack version $format")
            return format
        }

}