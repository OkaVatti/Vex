package dev.vex.client.world

import dev.vex.client.render.Camera
import dev.vex.client.render.TextureAtlas
import org.joml.Vector3f
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.abs

/**
 * Manages all chunks in the world with dynamic loading/unloading.
 */
class World {
    private val chunks = ConcurrentHashMap<ChunkPos, Chunk>()
    private val generator = WorldGenerator()

    private val renderDistance = 8 // chunks
    private val unloadDistance = renderDistance + 2

    fun loadChunk(chunkX: Int, chunkZ: Int): Chunk {
        val pos = ChunkPos(chunkX, chunkZ)
        return chunks.getOrPut(pos) {
            val chunk = Chunk(chunkX, chunkZ)
            generator.generateChunk(chunk)
            chunk
        }
    }

    fun getChunk(chunkX: Int, chunkZ: Int): Chunk? {
        return chunks[ChunkPos(chunkX, chunkZ)]
    }

    fun update(camera: Camera) {
        val playerChunkX = (camera.position.x / 16).toInt()
        val playerChunkZ = (camera.position.z / 16).toInt()

        // Load chunks around player
        for (x in playerChunkX - renderDistance..playerChunkX + renderDistance) {
            for (z in playerChunkZ - renderDistance..playerChunkZ + renderDistance) {
                if (getChunk(x, z) == null) {
                    loadChunk(x, z)
                }
            }
        }

        // Unload far chunks
        val toRemove = mutableListOf<ChunkPos>()
        for ((pos, _) in chunks) {
            val dx = abs(pos.x - playerChunkX)
            val dz = abs(pos.z - playerChunkZ)
            if (dx > unloadDistance || dz > unloadDistance) {
                toRemove.add(pos)
            }
        }

        for (pos in toRemove) {
            chunks.remove(pos)?.cleanup()
        }
    }

    fun generateMeshes(atlas: TextureAtlas) {
        for (chunk in chunks.values) {
            if (chunk.isDirty) {
                chunk.generateMesh(atlas)
            }
        }
    }

    fun render(camera: Camera) {
        val playerChunkX = (camera.position.x / 16).toInt()
        val playerChunkZ = (camera.position.z / 16).toInt()

        // Frustum culling (simplified)
        for (x in playerChunkX - renderDistance..playerChunkX + renderDistance) {
            for (z in playerChunkZ - renderDistance..playerChunkZ + renderDistance) {
                getChunk(x, z)?.render()
            }
        }
    }

    fun setBlock(worldX: Int, worldY: Int, worldZ: Int, blockId: Int) {
        val chunkX = worldX shr 4
        val chunkZ = worldZ shr 4
        val localX = worldX and 15
        val localZ = worldZ and 15

        getChunk(chunkX, chunkZ)?.setBlock(localX, worldY, localZ, blockId)
    }

    fun getBlock(worldX: Int, worldY: Int, worldZ: Int): Int {
        val chunkX = worldX shr 4
        val chunkZ = worldZ shr 4
        val localX = worldX and 15
        val localZ = worldZ and 15

        return getChunk(chunkX, chunkZ)?.getBlock(localX, worldY, localZ) ?: 0
    }

    fun cleanup() {
        chunks.values.forEach { it.cleanup() }
        chunks.clear()
    }
}

data class ChunkPos(val x: Int, val z: Int)
