package gg.aquatic.waves.chunk

import gg.aquatic.waves.Waves
import gg.aquatic.waves.module.WaveModules
import gg.aquatic.waves.module.WavesModule

object ChunkTracker : WavesModule {

    // WorldName, Chunk ID, List of players
    //val chunks = ConcurrentHashMap<String, ConcurrentHashMap<ChunkId, MutableSet<UUID>>>()
    //val playerToChunks = ConcurrentHashMap<UUID, Pair<String, MutableSet<ChunkId>>>()

    override val type: WaveModules = WaveModules.CHUNK_TRACKER

    override fun initialize(waves: Waves) {

    }

    override fun disable(waves: Waves) {

    }

}