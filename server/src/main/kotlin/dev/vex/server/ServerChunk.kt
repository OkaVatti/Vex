package dev.vex.server

/**
 * Simple in-memory chunk representation.
 *
 * Layout:
 *  - WIDTH x HEIGHT x DEPTH
 *  - index calculation: (y * DEPTH + z) * WIDTH + x
 *
 * This is intentionally minimal: getBlock/setBlock + metadata.
 */
class ServerChunk(
    val chunkX: Int,
    val chunkZ: Int
) {
    companion object {
        const val WIDTH = 16
        const val DEPTH = 16
        const val HEIGHT = 256 // total vertical height in blocks (pick value compatible with your design)
    }

    // Flat storage: WIDTH * HEIGHT * DEPTH
    private val blocks = IntArray(WIDTH * HEIGHT * DEPTH)

    var state: ChunkState = ChunkState.NEW

    private fun idx(x: Int, y: Int, z: Int): Int {
        require(x in 0 until WIDTH) { "x out of range: $x" }
        require(y in 0 until HEIGHT) { "y out of range: $y" }
        require(z in 0 until DEPTH) { "z out of range: $z" }
        return (y * DEPTH + z) * WIDTH + x
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        if (x !in 0 until WIDTH || y !in 0 until HEIGHT || z !in 0 until DEPTH) return 0
        return blocks[idx(x, y, z)]
    }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        if (x !in 0 until WIDTH || y !in 0 until HEIGHT || z !in 0 until DEPTH) return
        blocks[idx(x, y, z)] = id
    }
}
