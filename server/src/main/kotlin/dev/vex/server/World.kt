package dev.vex.server

/**
 * Minimal world that can generate a chunk synchronously for demo/testing.
 * Replace generation with your real generator & async pipeline later.
 */
class World(val seed: Long = 0L) {

    /**
     * Synchronously create and generate a chunk at (cx, cz).
     * In a real server this would be async, cached, load-from-disk, etc.
     */
    fun getOrLoadChunk(cx: Int, cz: Int): ServerChunk {
        val chunk = ServerChunk(cx, cz)
        generateSimple(chunk)
        return chunk
    }

    private fun generateSimple(chunk: ServerChunk) {
        // Very small example generator:
        // - ground layer: y == 64 => block id 3
        // - rock under that: y < 64 => block id 2
        // - air else (0)
        val groundY = 64
        for (y in 0 until ServerChunk.HEIGHT) {
            for (z in 0 until ServerChunk.DEPTH) {
                for (x in 0 until ServerChunk.WIDTH) {
                    val id = when {
                        y < groundY - 4 -> 2 // stone
                        y < groundY -> 1     // dirt
                        y == groundY -> 3    // grass/top
                        else -> 0             // air
                    }
                    chunk.setBlock(x, y, z, id)
                }
            }
        }

        // mark intermediate state then ready
        chunk.state = ChunkState.GENERATED
        // Normally you'd run lighting / meshing here
        chunk.state = ChunkState.READY
    }

    fun shutdown() {
        // placeholder for resource cleanup
    }
}
