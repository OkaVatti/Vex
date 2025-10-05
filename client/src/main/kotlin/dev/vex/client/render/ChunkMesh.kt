package dev.vex.client.render

import dev.vex.client.world.Blocks
import dev.vex.client.world.Chunk
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

/**
 * Generates and stores the renderable mesh for a single Chunk.
 * This implementation uses a greedy meshing algorithm to reduce the vertex count.
 */
class ChunkMesh(private val chunk: Chunk) {
    private var vao = 0
    private var vbo = 0
    private var vertexCount = 0

    private val vertices = mutableListOf<Float>()

    private enum class Face { NORTH, SOUTH, EAST, WEST, TOP, BOTTOM }

    fun generate(atlas: TextureAtlas) {
        vertices.clear()

        // Generate mesh for each of the 6 faces
        for (face in Face.values()) {
            generateFace(face, atlas)
        }

        uploadToGPU()
    }

    private fun generateFace(face: Face, atlas: TextureAtlas) {
        val (dirX, dirY, dirZ) = when (face) {
            Face.NORTH -> Triple(0, 0, -1)
            Face.SOUTH -> Triple(0, 0, 1)
            Face.EAST  -> Triple(1, 0, 0)
            Face.WEST  -> Triple(-1, 0, 0)
            Face.TOP   -> Triple(0, 1, 0)
            Face.BOTTOM-> Triple(0, -1, 0)
        }

        // Determine the axes for our 2D slice
        val (sliceW, sliceH, sliceD) = getAxisDimensions(face)

        // Iterate through each layer of the chunk's 3D volume
        for (layer in 0 until sliceD) {
            // Mask stores blockID (or 0)
            val mask = Array(sliceW) { IntArray(sliceH) }

            // 1. Create the mask for the current slice
            for (x in 0 until sliceW) {
                for (y in 0 until sliceH) {
                    val (pX, pY, pZ) = mapToChunkCoords(x, y, layer, face)

                    val currentBlockId = chunk.getBlock(pX, pY, pZ)
                    val neighborBlockId = chunk.getBlock(pX + dirX, pY + dirY, pZ + dirZ)

                    val currentBlock = Blocks.getById(currentBlockId)
                    val neighborBlock = Blocks.getById(neighborBlockId)

                    // A face is visible if the neighbor is transparent and this block is not.
                    // This prevents rendering faces between two transparent blocks (e.g. water-glass).
                    if (currentBlock.id != Blocks.AIR.id && !currentBlock.transparent && neighborBlock.transparent) {
                        mask[x][y] = currentBlockId
                    } else {
                        mask[x][y] = 0
                    }
                }
            }

            // 2. Generate quads from the mask using the greedy algorithm
            var y = 0
            while (y < sliceH) {
                var x = 0
                while (x < sliceW) {
                    val blockId = mask[x][y]
                    if (blockId == 0) {
                        x++
                        continue
                    }

                    // Find the width of the quad
                    var w = 1
                    while (x + w < sliceW && mask[x + w][y] == blockId) {
                        w++
                    }

                    // Find the height of the quad
                    var h = 1
                    var done = false
                    while (y + h < sliceH && !done) {
                        for (k in 0 until w) {
                            if (mask[x + k][y + h] != blockId) {
                                done = true
                                break
                            }
                        }
                        if (!done) h++
                    }

                    // Add the quad to the vertices list
                    addQuad(x, y, layer, w, h, face, blockId, atlas)

                    // Zero out the mask for the area covered by this quad
                    for (j in 0 until h) {
                        for (i in 0 until w) {
                            mask[x + i][y + j] = 0
                        }
                    }

                    x += w
                }
                y++
            }
        }
    }

    /**
     * Rewritten to correctly generate quads for the face and compute UVs from textureX/textureY.
     */
    private fun addQuad(
        sliceX: Int, sliceY: Int, layer: Int,
        width: Int, height: Int,
        face: Face, blockId: Int, atlas: TextureAtlas
    ) {
        val block = Blocks.getById(blockId)
        // compute texture index (tile index in atlas)
        val textureIndex = block.textureIndex()
        // tile size in atlas
        val tilesPerRow = 16f
        val u0 = (textureIndex % tilesPerRow) / tilesPerRow
        val v0 = (textureIndex / tilesPerRow) / tilesPerRow
        val uSize = 1f / tilesPerRow
        val vSize = 1f / tilesPerRow

        // For quads that span multiple tiles we extend uv by the width/height in tiles
        val u1 = u0 + uSize * width
        val v1 = v0 + vSize * height

        // Simple AO placeholder (all 1.0)
        val ao = floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f)

        val x1 = sliceX.toFloat()
        val y1 = sliceY.toFloat()
        val x2 = (sliceX + width).toFloat()
        val y2 = (sliceY + height).toFloat()
        val l = layer.toFloat()

        when (face) {
            Face.TOP -> { // +Y
                val y = l + 1f - Chunk.MIN_Y
                // Triangles using two triangles (6 vertices)
                addVertex(x1, y, y1, u0, v0, ao[0], 0f, 1f, 0f)
                addVertex(x1, y, y2, u0, v1, ao[3], 0f, 1f, 0f)
                addVertex(x2, y, y2, u1, v1, ao[2], 0f, 1f, 0f)

                addVertex(x1, y, y1, u0, v0, ao[0], 0f, 1f, 0f)
                addVertex(x2, y, y2, u1, v1, ao[2], 0f, 1f, 0f)
                addVertex(x2, y, y1, u1, v0, ao[1], 0f, 1f, 0f)
            }
            Face.BOTTOM -> { // -Y
                val y = l.toFloat() - Chunk.MIN_Y
                addVertex(x1, y, y1, u0, v0, ao[0], 0f, -1f, 0f)
                addVertex(x2, y, y1, u1, v0, ao[1], 0f, -1f, 0f)
                addVertex(x2, y, y2, u1, v1, ao[2], 0f, -1f, 0f)

                addVertex(x1, y, y1, u0, v0, ao[0], 0f, -1f, 0f)
                addVertex(x2, y, y2, u1, v1, ao[2], 0f, -1f, 0f)
                addVertex(x1, y, y2, u0, v1, ao[3], 0f, -1f, 0f)
            }
            Face.NORTH -> { // -Z
                val z = l.toFloat()
                addVertex(x1, y1, z, u0, v0, ao[0], 0f, 0f, -1f)
                addVertex(x1, y2, z, u0, v1, ao[3], 0f, 0f, -1f)
                addVertex(x2, y2, z, u1, v1, ao[2], 0f, 0f, -1f)

                addVertex(x1, y1, z, u0, v0, ao[0], 0f, 0f, -1f)
                addVertex(x2, y2, z, u1, v1, ao[2], 0f, 0f, -1f)
                addVertex(x2, y1, z, u1, v0, ao[1], 0f, 0f, -1f)
            }
            Face.SOUTH -> { // +Z
                val z = l.toFloat() + 1f
                addVertex(x1, y1, z, u0, v0, ao[0], 0f, 0f, 1f)
                addVertex(x2, y1, z, u1, v0, ao[1], 0f, 0f, 1f)
                addVertex(x2, y2, z, u1, v1, ao[2], 0f, 0f, 1f)

                addVertex(x1, y1, z, u0, v0, ao[0], 0f, 0f, 1f)
                addVertex(x2, y2, z, u1, v1, ao[2], 0f, 0f, 1f)
                addVertex(x1, y2, z, u0, v1, ao[3], 0f, 0f, 1f)
            }
            Face.EAST -> { // +X
                val x = l.toFloat() + 1f
                addVertex(x, y1, x1, u0, v0, ao[0], 1f, 0f, 0f)
                addVertex(x, y1, x2, u1, v0, ao[1], 1f, 0f, 0f)
                addVertex(x, y2, x2, u1, v1, ao[2], 1f, 0f, 0f)

                addVertex(x, y1, x1, u0, v0, ao[0], 1f, 0f, 0f)
                addVertex(x, y2, x2, u1, v1, ao[2], 1f, 0f, 0f)
                addVertex(x, y2, x1, u0, v1, ao[3], 1f, 0f, 0f)
            }
            Face.WEST -> { // -X
                val x = l.toFloat()
                addVertex(x, y1, x1, u0, v0, ao[0], -1f, 0f, 0f)
                addVertex(x, y2, x1, u0, v1, ao[3], -1f, 0f, 0f)
                addVertex(x, y2, x2, u1, v1, ao[2], -1f, 0f, 0f)

                addVertex(x, y1, x1, u0, v0, ao[0], -1f, 0f, 0f)
                addVertex(x, y2, x2, u1, v1, ao[2], -1f, 0f, 0f)
                addVertex(x, y1, x2, u1, v0, ao[1], -1f, 0f, 0f)
            }
        }
    }

    private fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, ao: Float, nx: Float, ny: Float, nz: Float) {
        // Position (in world space)
        vertices.add(x + chunk.x * Chunk.WIDTH)
        vertices.add(y)
        vertices.add(z + chunk.z * Chunk.DEPTH)
        // Texture Coords
        vertices.add(u)
        vertices.add(v)
        // Ambient Occlusion
        vertices.add(ao)
        // Normal Vector
        vertices.add(nx)
        vertices.add(ny)
        vertices.add(nz)
    }

    private fun getAxisDimensions(face: Face): Triple<Int, Int, Int> {
        return when (face) {
            Face.TOP, Face.BOTTOM -> Triple(Chunk.WIDTH, Chunk.DEPTH, Chunk.HEIGHT)
            Face.NORTH, Face.SOUTH -> Triple(Chunk.WIDTH, Chunk.HEIGHT, Chunk.DEPTH)
            Face.EAST, Face.WEST -> Triple(Chunk.DEPTH, Chunk.HEIGHT, Chunk.WIDTH)
        }
    }

    private fun mapToChunkCoords(x: Int, y: Int, layer: Int, face: Face): Triple<Int, Int, Int> {
        return when (face) {
            Face.TOP, Face.BOTTOM -> Triple(x, layer + Chunk.MIN_Y, y)
            Face.NORTH, Face.SOUTH -> Triple(x, y + Chunk.MIN_Y, layer)
            Face.EAST, Face.WEST -> Triple(layer, y + Chunk.MIN_Y, x)
        }
    }

    private fun uploadToGPU() {
        if (vertices.isEmpty()) {
            vertexCount = 0
            return
        }

        if (vao == 0) vao = glGenVertexArrays()
        glBindVertexArray(vao)

        if (vbo == 0) vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        val buffer: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
        buffer.put(vertices.toFloatArray()).flip()

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)
        MemoryUtil.memFree(buffer)

        // Vertex format: 3 pos, 2 uv, 1 ao, 3 normal (9 floats total)
        val stride = 9 * Float.SIZE_BYTES
        // Position
        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0)
        glEnableVertexAttribArray(0)
        // Texture UV
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, (3 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(1)
        // Ambient Occlusion
        glVertexAttribPointer(2, 1, GL_FLOAT, false, stride, (5 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(2)
        // Normal
        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, (6 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(3)

        glBindVertexArray(0)

        vertexCount = vertices.size / 9
    }

    fun render() {
        if (vao == 0 || vertexCount == 0) return

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
        glBindVertexArray(0)
    }

    fun cleanup() {
        if (vbo != 0) {
            glDeleteBuffers(vbo)
            vbo = 0
        }
        if (vao != 0) {
            glDeleteVertexArrays(vao)
            vao = 0
        }
        vertexCount = 0
    }
}
