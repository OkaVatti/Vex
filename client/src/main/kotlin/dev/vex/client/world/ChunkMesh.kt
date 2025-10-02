package dev.vex.client.render

import dev.vex.client.world.Chunk
import dev.vex.client.world.Blocks
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import java.nio.FloatBuffer

/**
 * Generates optimized mesh for a chunk using greedy meshing algorithm.
 */
class ChunkMesh(private val chunk: Chunk) {
    private var vao = 0
    private var vbo = 0
    private var vertexCount = 0

    private val vertices = mutableListOf<Float>()

    private enum class Face { NORTH, SOUTH, EAST, WEST, TOP, BOTTOM }

    fun generate(atlas: TextureAtlas) {
        vertices.clear()

        for (face in Face.entries) {
            generateFace(face, atlas)
        }

        uploadToGPU()
    }

    private fun generateFace(face: Face, atlas: TextureAtlas) {
        val (dx, dy, dz) = when (face) {
            Face.NORTH -> Triple(0, 0, -1)
            Face.SOUTH -> Triple(0, 0, 1)
            Face.EAST -> Triple(1, 0, 0)
            Face.WEST -> Triple(-1, 0, 0)
            Face.TOP -> Triple(0, 1, 0)
            Face.BOTTOM -> Triple(0, -1, 0)
        }

        val (w, h, d) = getAxisDimensions(face)

        for (layer in 0 until d) {
            val mask = Array(w) { BooleanArray(h) }

            for (x in 0 until w) {
                for (y in 0 until h) {
                    val (bx, by, bz) = mapToWorld(x, y, layer, face)

                    if (bx !in 0 until chunk.width ||
                        by !in -128 until 256 ||
                        bz !in 0 until chunk.depth) {
                        continue
                    }

                    val block = chunk.getBlock(bx, by, bz)
                    if (block == 0) continue

                    val neighborBlock = chunk.getBlockSafe(bx + dx, by + dy, bz + dz)
                    mask[x][y] = neighborBlock == 0 || !isOpaque(neighborBlock)
                }
            }

            for (x in 0 until w) {
                for (y in 0 until h) {
                    if (!mask[x][y]) continue

                    var width = 1
                    while (x + width < w && mask[x + width][y]) {
                        width++
                    }

                    var height = 1
                    var done = false
                    while (y + height < h && !done) {
                        for (k in 0 until width) {
                            if (!mask[x + k][y + height]) {
                                done = true
                                break
                            }
                        }
                        if (!done) height++
                    }

                    val (bx, by, bz) = mapToWorld(x, y, layer, face)
                    val blockId = chunk.getBlock(bx, by, bz)

                    addQuad(x, y, layer, width, height, face, blockId, atlas)

                    for (i in 0 until width) {
                        for (j in 0 until height) {
                            mask[x + i][y + j] = false
                        }
                    }
                }
            }
        }
    }

    private fun addQuad(
        x: Int, y: Int, z: Int,
        w: Int, h: Int,
        face: Face,
        blockId: Int,
        atlas: TextureAtlas
    ) {
        val corners = getQuadCorners(x, y, z, w, h, face)
        val x0 = corners[0]
        val y0 = corners[1]
        val z0 = corners[2]
        val x1 = corners[3]
        val y1 = corners[4]
        val z1 = corners[5]

        val textureIndex = getTextureIndex(blockId, face)
        val uv = atlas.getUV(textureIndex)

        val u0 = uv[0]
        val v0 = uv[1]
        val u1 = uv[0] + (uv[2] - uv[0]) * w
        val v1 = uv[1] + (uv[3] - uv[1]) * h

        val ao = getAO(face)

        when (face) {
            Face.TOP, Face.SOUTH, Face.EAST -> {
                addVertex(x0, y0, z0, u0, v0, ao[0], face)
                addVertex(x1, y0, z0, u1, v0, ao[1], face)
                addVertex(x1, y1, z1, u1, v1, ao[2], face)
                addVertex(x0, y0, z0, u0, v0, ao[0], face)
                addVertex(x1, y1, z1, u1, v1, ao[2], face)
                addVertex(x0, y1, z1, u0, v1, ao[3], face)
            }
            else -> {
                addVertex(x0, y0, z0, u0, v0, ao[0], face)
                addVertex(x0, y1, z1, u0, v1, ao[3], face)
                addVertex(x1, y1, z1, u1, v1, ao[2], face)
                addVertex(x0, y0, z0, u0, v0, ao[0], face)
                addVertex(x1, y1, z1, u1, v1, ao[2], face)
                addVertex(x1, y0, z0, u1, v0, ao[1], face)
            }
        }
    }

    private fun addVertex(x: Float, y: Float, z: Float, u: Float, v: Float, ao: Float, face: Face) {
        vertices.add(x + chunk.x * 16f)
        vertices.add(y)
        vertices.add(z + chunk.z * 16f)
        vertices.add(u)
        vertices.add(v)
        vertices.add(ao)

        val normal = when (face) {
            Face.TOP -> Triple(0f, 1f, 0f)
            Face.BOTTOM -> Triple(0f, -1f, 0f)
            Face.NORTH -> Triple(0f, 0f, -1f)
            Face.SOUTH -> Triple(0f, 0f, 1f)
            Face.EAST -> Triple(1f, 0f, 0f)
            Face.WEST -> Triple(-1f, 0f, 0f)
        }
        vertices.add(normal.first)
        vertices.add(normal.second)
        vertices.add(normal.third)
    }

    private fun getQuadCorners(x: Int, y: Int, z: Int, w: Int, h: Int, face: Face): FloatArray {
        return when (face) {
            Face.TOP -> floatArrayOf(
                x.toFloat(), (y + 1).toFloat(), (z + 1).toFloat(),
                (x + w).toFloat(), (y + 1).toFloat(), z.toFloat()
            )
            Face.BOTTOM -> floatArrayOf(
                x.toFloat(), y.toFloat(), z.toFloat(),
                (x + w).toFloat(), y.toFloat(), (z + 1).toFloat()
            )
            Face.NORTH -> floatArrayOf(
                x.toFloat(), y.toFloat(), z.toFloat(),
                (x + w).toFloat(), (y + h).toFloat(), z.toFloat()
            )
            Face.SOUTH -> floatArrayOf(
                x.toFloat(), y.toFloat(), (z + 1).toFloat(),
                (x + w).toFloat(), (y + h).toFloat(), (z + 1).toFloat()
            )
            Face.EAST -> floatArrayOf(
                (x + 1).toFloat(), y.toFloat(), z.toFloat(),
                (x + 1).toFloat(), (y + h).toFloat(), (z + w).toFloat()
            )
            Face.WEST -> floatArrayOf(
                x.toFloat(), y.toFloat(), z.toFloat(),
                x.toFloat(), (y + h).toFloat(), (z + w).toFloat()
            )
        }
    }

    private fun getAO(face: Face): FloatArray {
        return floatArrayOf(0.85f, 0.85f, 0.85f, 0.85f)
    }

    private fun getAxisDimensions(face: Face): Triple<Int, Int, Int> {
        return when (face) {
            Face.TOP, Face.BOTTOM -> Triple(chunk.width, chunk.depth, chunk.height)
            Face.NORTH, Face.SOUTH -> Triple(chunk.width, chunk.height, chunk.depth)
            Face.EAST, Face.WEST -> Triple(chunk.depth, chunk.height, chunk.width)
        }
    }

    private fun mapToWorld(x: Int, y: Int, layer: Int, face: Face): Triple<Int, Int, Int> {
        return when (face) {
            Face.TOP, Face.BOTTOM -> Triple(x, layer - 128, y)
            Face.NORTH, Face.SOUTH -> Triple(x, y - 128, layer)
            Face.EAST, Face.WEST -> Triple(layer, y - 128, x)
        }
    }

    private fun getTextureIndex(blockId: Int, face: Face): Int {
        val block = Blocks.getById(blockId)
        return when (face) {
            Face.TOP -> block.topTexture
            Face.BOTTOM -> block.bottomTexture
            else -> block.sideTexture
        }
    }

    private fun isOpaque(blockId: Int): Boolean {
        return Blocks.getById(blockId).isOpaque
    }

    private fun uploadToGPU() {
        if (vertices.isEmpty()) return

        vao = glGenVertexArrays()
        glBindVertexArray(vao)

        vbo = glGenBuffers()
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        val buffer: FloatBuffer = MemoryUtil.memAllocFloat(vertices.size)
        buffer.put(vertices.toFloatArray()).flip()

        glBufferData(GL_ARRAY_BUFFER, buffer, GL_STATIC_DRAW)

        val stride = 9 * Float.SIZE_BYTES

        glVertexAttribPointer(0, 3, GL_FLOAT, false, stride, 0)
        glEnableVertexAttribArray(0)

        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, (3 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(1)

        glVertexAttribPointer(2, 1, GL_FLOAT, false, stride, (5 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(2)

        glVertexAttribPointer(3, 3, GL_FLOAT, false, stride, (6 * Float.SIZE_BYTES).toLong())
        glEnableVertexAttribArray(3)

        glBindVertexArray(0)

        vertexCount = vertices.size / 9

        MemoryUtil.memFree(buffer)
    }

    fun render() {
        if (vao == 0 || vertexCount == 0) return

        glBindVertexArray(vao)
        glDrawArrays(GL_TRIANGLES, 0, vertexCount)
        glBindVertexArray(0)
    }

    fun cleanup() {
        if (vbo != 0) glDeleteBuffers(vbo)
        if (vao != 0) glDeleteVertexArrays(vao)
    }
}