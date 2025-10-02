package dev.vex.client.world

import dev.vex.client.render.ChunkMesh
import dev.vex.client.render.TextureAtlas

/**
 * Represents a 16x384x16 chunk of blocks, using a Y-range of -128 to 255.
 * It is vertically divided into 24 ChunkSections (16x16x16).
 *
 * @param x The chunk's X coordinate (in chunk units).
 * @param z The chunk's Z coordinate (in chunk units).
 */
class Chunk(val x: Int, val z: Int) {
    companion object {
        const val WIDTH = 16
        const val HEIGHT = 384
        const val DEPTH = 16
        const val MAX_Y = 255
        const val MIN_Y = -128
        const val SECTIONS = HEIGHT / 16
    }

    // A chunk is made of vertical 16x16x16 sections
    private val sections = Array(SECTIONS) { ChunkSection() }

    var mesh: ChunkMesh? = null
        private set

    // A "dirty" chunk needs its mesh to be rebuilt
    var isDirty = true
        private set

    enum class ChunkState {
        UNLOADED, LOADING, GENERATED, DECORATED, LIT, READY, SAVING
    }

    var state = ChunkState.UNLOADED

    /**
     * Sets a block in the chunk using world Y-coordinates.
     */
    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        if (x !in 0 until WIDTH || y !in MIN_Y..MAX_Y || z !in 0 until DEPTH) return

        // Convert world Y to 0-indexed chunk Y
        val chunkY = y - MIN_Y
        val sectionIndex = chunkY / 16
        val localY = chunkY % 16

        sections[sectionIndex].setBlock(x, localY, z, id)
        isDirty = true
    }

    /**
     * Gets a block from the chunk using world Y-coordinates. Returns AIR (0) for out-of-bounds.
     */
    fun getBlock(x: Int, y: Int, z: Int): Int {
        if (x !in 0 until WIDTH || y !in MIN_Y..MAX_Y || z !in 0 until DEPTH) {
            return 0 // Return AIR for out-of-bounds access
        }

        val chunkY = y - MIN_Y
        val sectionIndex = chunkY / 16
        val localY = chunkY % 16

        return sections[sectionIndex].getBlock(x, localY, z)
    }

    /**
     * Generates the visual mesh for this chunk if it's marked as dirty.
     */
    fun generateMesh(atlas: TextureAtlas) {
        if (!isDirty && mesh != null) return

        mesh?.cleanup() // Clean up old mesh if it exists
        mesh = ChunkMesh(this).apply {
            generate(atlas)
        }

        isDirty = false
    }

    /**
     * Renders the chunk's mesh.
     */
    fun render() {
        mesh?.render()
    }

    /**
     * Finds the Y-coordinate of the highest non-air block at a given column.
     */
    fun getHeightAt(x: Int, z: Int): Int {
        for (y in MAX_Y downTo MIN_Y) {
            if (getBlock(x, y, z) != Blocks.AIR.id) {
                return y
            }
        }
        return MIN_Y
    }

    /**
     * Releases the GPU resources used by the chunk's mesh.
     */
    fun cleanup() {
        mesh?.cleanup()
    }
}

/**
 * A 16x16x16 section of blocks within a chunk.
 */
class ChunkSection {
    // 16*16*16 = 4096 blocks
    private val blocks = IntArray(16 * 16 * 16) { 0 }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        blocks[getIndex(x, y, z)] = id
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        return blocks[getIndex(x, y, z)]
    }

    /**
     * Converts 3D local coordinates to a 1D array index.
     */
    private fun getIndex(x: Int, y: Int, z: Int): Int {
        return (y * 16 + z) * 16 + x
    }
}