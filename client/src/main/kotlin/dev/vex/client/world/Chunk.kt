package dev.vex.client.world

class Chunk(val x: Int, val z: Int) {
    val width = 16
    val height = 256
    val depth = 16

    val blocks = Array(width) { Array(height) { Array(depth) { 0 } } }

    fun setBlock(x: Int, y: Int, z: Int, id: Int) {
        blocks[x][y][z] = id
    }

    fun getBlock(x: Int, y: Int, z: Int): Int {
        return blocks[x][y][z]
    }
}
