package gg.aquatic.waves.hologram

import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule
import gg.aquatic.waves.util.runAsyncTimer
import java.util.concurrent.ConcurrentHashMap

object HologramHandler: WavesModule {

    override val type: WaveModules = WaveModules.HOLOGRAMS

    val spawnedHolograms = ConcurrentHashMap.newKeySet<AquaticHologram>()

    override fun initialize(waves: Waves) {
        runAsyncTimer(1,1) {
            for (hologram in spawnedHolograms) {
                hologram.tick()
            }
        }
    }

    override fun disable(waves: Waves) {

    }
}