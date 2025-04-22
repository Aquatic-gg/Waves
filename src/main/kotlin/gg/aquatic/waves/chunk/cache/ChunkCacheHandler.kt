package gg.aquatic.waves.chunk.cache

import org.bukkit.Chunk
import org.bukkit.World

object ChunkCacheHandler {

    // World, ChunkX ChunkY, Object
    val cache = HashMap<String, MutableMap<String, MutableMap<Class<ChunkObject>, ChunkObject>>>()

    fun getObjects(x: Int, z: Int, world: World): MutableMap<Class<ChunkObject>, ChunkObject> {
        val worldName = world.name
        val chunkMap = cache[worldName] ?: return HashMap()
        val chunkId = "${x},${z}"
        val map = chunkMap[chunkId] ?: return HashMap()
        return map
    }

    fun getObject(chunk: Chunk, clazz: Class<out ChunkObject>): ChunkObject? {
        return getObjects(chunk.x, chunk.z, chunk.world)[clazz]
    }
    fun getObject(x: Int, z: Int, world: World, clazz: Class<out ChunkObject>): ChunkObject? {
        return getObjects(x, z, world)[clazz]
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