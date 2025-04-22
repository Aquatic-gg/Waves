package gg.aquatic.waves.chunk.cache

import org.bukkit.Chunk

object ChunkCacheHandler {

    // World, ChunkX ChunkY, Object
    val cache = HashMap<String, MutableMap<String, MutableMap<Class<ChunkObject>, ChunkObject>>>()

    fun getObjects(chunk: Chunk): MutableMap<Class<ChunkObject>, ChunkObject> {
        val world = chunk.world.name
        val chunkMap = cache[world] ?: return HashMap()
        val chunkId = "${chunk.x},${chunk.z}"
        val map = chunkMap[chunkId] ?: return HashMap()
        return map
    }

    fun getObject(chunk: Chunk, clazz: Class<out ChunkObject>): ChunkObject? {
        return getObjects(chunk)[clazz]
    }

    fun registerObject(obj: ChunkObject, chunk: Chunk): ChunkObject? {
        val world = chunk.world.name
        val chunkMap = cache.getOrPut(world) { HashMap() }
        val chunkId = "${chunk.x},${chunk.z}"
        val objectMap = chunkMap.getOrPut(chunkId) { HashMap() }
        objectMap[obj.javaClass] = obj
        return objectMap.put(obj.javaClass,obj)
    }

    fun unregisterObject(clazz: Class<ChunkObject>, chunk: Chunk): ChunkObject? {
        val world = chunk.world.name
        val chunkMap = cache[world] ?: return null
        val chunkId = "${chunk.x},${chunk.z}"
        val objectMap = chunkMap[chunkId] ?: return null
        return objectMap.remove(clazz)
    }


}