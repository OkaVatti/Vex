package dev.vex.client.render

import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30.*
import java.nio.ByteBuffer

/**
 * Manages texture atlases for blocks and items.
 * Each atlas is 512x512 with 16x16 textures in a 32x32 grid.
 */
class TextureAtlas(private val path: String) {
    var textureId: Int = 0
        private set

    var width: Int = 0
        private set

    var height: Int = 0
        private set

    // Atlas dimensions
    private val atlasSize = 512
    private val textureSize = 16
    private val gridSize = atlasSize / textureSize // 32x32 grid

    fun load() {
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            // Load image with STB
            val imageBuffer: ByteBuffer? = STBImage.stbi_load(path, w, h, channels, 4)
            if (imageBuffer == null) {
                throw RuntimeException("Failed to load texture: $path\n${STBImage.stbi_failure_reason()}")
            }

            width = w.get(0)
            height = h.get(0)

            // Generate OpenGL texture
            textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)

            // Set texture parameters for pixel-perfect rendering
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_LINEAR)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

            // Upload texture data
            glTexImage2D(
                GL_TEXTURE_2D, 0, GL_RGBA, width, height,
                0, GL_RGBA, GL_UNSIGNED_BYTE, imageBuffer
            )

            // Generate mipmaps for better distance rendering
            glGenerateMipmap(GL_TEXTURE_2D)

            // Free image buffer
            STBImage.stbi_image_free(imageBuffer)
        }
    }

    /**
     * Get UV coordinates for a texture index in the atlas.
     * Returns [u0, v0, u1, v1] for the texture rectangle.
     */
    fun getUV(textureIndex: Int): FloatArray {
        val x = textureIndex % gridSize
        val y = textureIndex / gridSize

        val u0 = x.toFloat() / gridSize
        val v0 = y.toFloat() / gridSize
        val u1 = (x + 1).toFloat() / gridSize
        val v1 = (y + 1).toFloat() / gridSize

        return floatArrayOf(u0, v0, u1, v1)
    }

    /**
     * Bind this texture atlas for rendering.
     */
    fun bind() {
        glBindTexture(GL_TEXTURE_2D, textureId)
    }

    fun cleanup() {
        glDeleteTextures(textureId)
    }

    companion object {
        /**
         * Creates a placeholder texture atlas with simple colored blocks
         * Used when the actual PNG file isn't available
         */
        fun createPlaceholder(): TextureAtlas {
            val atlas = TextureAtlas("placeholder")

            // Create 512x512 texture with colored blocks
            val size = 512
            val tileSize = 16
            val buffer = java.nio.ByteBuffer.allocateDirect(size * size * 4)

            // Fill with colored tiles
            for (y in 0 until size) {
                for (x in 0 until size) {
                    val tileX = x / tileSize
                    val tileY = y / tileSize
                    val tileIndex = tileY * 32 + tileX

                    // Generate color based on tile index
                    val color = getPlaceholderColor(tileIndex)
                    buffer.put(color[0].toByte())
                    buffer.put(color[1].toByte())
                    buffer.put(color[2].toByte())
                    buffer.put(255.toByte())
                }
            }
            buffer.flip()

            // Upload to OpenGL
            atlas.textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, atlas.textureId)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_REPEAT)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_REPEAT)

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, size, size, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

            atlas.width = size
            atlas.height = size

            return atlas
        }

        private fun getPlaceholderColor(index: Int): IntArray {
            return when (index) {
                0 -> intArrayOf(124, 252, 0)    // Grass - bright green
                1 -> intArrayOf(128, 128, 128)  // Stone - gray
                2 -> intArrayOf(139, 69, 19)    // Dirt - brown
                3 -> intArrayOf(124, 252, 0)    // Grass side top
                16 -> intArrayOf(105, 105, 105) // Cobble - dark gray
                17 -> intArrayOf(64, 64, 64)    // Bedrock - very dark
                18 -> intArrayOf(238, 214, 175) // Sand - tan
                48 -> intArrayOf(50, 50, 50)    // Coal ore - black specks
                49 -> intArrayOf(184, 115, 51)  // Copper ore - orange-brown
                50 -> intArrayOf(192, 192, 192) // Tin ore - silver
                55 -> intArrayOf(210, 180, 140) // Iron ore - tan
                else -> {
                    // Generate pseudo-random color
                    val r = ((index * 123) % 200) + 55
                    val g = ((index * 456) % 200) + 55
                    val b = ((index * 789) % 200) + 55
                    intArrayOf(r, g, b)
                }
            }
        }
    }
}