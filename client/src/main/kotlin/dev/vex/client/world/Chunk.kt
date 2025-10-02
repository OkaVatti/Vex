package dev.vex.client.world

import dev.vex.client.render.ChunkMesh
import dev.vex.client.render.TextureAtlas

/**
 * Represents a 16x384x16 chunk of blocks.
 */

class Chunk(val x: Int, val z: Int) {
    val width = 16
    val height = 384
    val depth = 16

    private val sections = Array(24) { ChunkSection() }

    var mesh: ChunkMesh? = null
        private set

    var isDirty = true
        private set

    enum class ChunkState {
        UNLOADED, LOADING, GENERATED, DECORATED, LIT, READY, SAVING
    }

    data class Chunk(val chunkX: Int, val chunkZ: Int) {
        val position get() = ChunkPos(chunkX, chunkZ)
    }


    var state = ChunkState.UNLOADED

    /**
     * Set a block in the chunk (world Y coordinates).
     */
    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        if (x !in 0 until width || y !in -128 until 256 || z !in 0 until depth) return

        val worldY = y + 128
        val sectionIndex = worldY / 16
        val localY = worldY % 16

        sections[sectionIndex].setBlock(x, localY, z, id)
        isDirty = true
    }

    /**
     * Get a block from the chunk (world Y coordinates).
     */
    fun getBlock(x: Int, y: Int, z: Int): Int {
        if (x !in 0 until width || y !in -128 until 256 || z !in 0 until depth) return 0

        val worldY = y + 128
        val sectionIndex = worldY / 16
        val localY = worldY % 16

        return sections[sectionIndex].getBlock(x, localY, z)
    }

    /**
     * Safe get block that returns air for out of bounds.
     */
    fun getBlockSafe(x: Int, y: Int, z: Int): Int {
        return if (x in 0 until width && y in -128 until 256 && z in 0 until depth) {
            getBlock(x, y, z)
        } else 0
    }

    /**
     * Generate mesh for rendering.
     */
    fun generateMesh(atlas: TextureAtlas) {
        if (!isDirty && mesh != null) return

        mesh?.cleanup()
        mesh = ChunkMesh(this).apply {
            generate(atlas)
        }

        isDirty = false
    }

    /**
     * Render the chunk mesh.
     */
    fun render() {
        mesh?.render()
    }

    /**
     * Get the highest non-air block at x, z.
     */
    fun getHeightAt(x: Int, z: Int): Int {
        for (y in 255 downTo -128) {
            if (getBlock(x, y, z) != 0) {
                return y
            }
        }
        return -128
    }

    fun cleanup() {
        mesh?.cleanup()
    }
}

/**
 * A 16x16x16 section of blocks within a chunk.
 */
class ChunkSection {
    private val blocks = IntArray(16 * 16 * 16) { 0 }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        blocks[getIndex(x, y, z)] = id
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        return blocks[getIndex(x, y, z)]
    }

    private fun getIndex(x: Int, y: Int, z: Int): Int {
        return (y * 16 + z) * 16 + x
    }
}