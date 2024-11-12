package gg.aquatic.waves.fake

import gg.aquatic.aquaticseries.lib.chunkcache.ChunkObject
import gg.aquatic.waves.fake.block.FakeEntity
import gg.aquatic.waves.fake.entity.FakeBlock
import java.util.concurrent.ConcurrentHashMap

class FakeObjectChunkBundle: ChunkObject {

    val blocks = ConcurrentHashMap.newKeySet<FakeBlock>()
    val entities = ConcurrentHashMap.newKeySet<FakeEntity>()

}