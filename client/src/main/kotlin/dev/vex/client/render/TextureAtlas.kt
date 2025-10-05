package dev.vex.client.render

import dev.vex.client.util.ResourceLoader
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

/**
 * Handles loading and managing a texture atlas (a single image containing multiple smaller textures).
 */

class TextureAtlas(filepath: String) {
    private var textureId: Int = 0
    private var textureWidth: Int = 0
    private var textureHeight: Int = 0
    private val cellSizePx = 16
    private var cellsPerRow: Int = 0
    private var cellsPerCol: Int = 0

    // The filepath should now be the clean, correct classpath path (e.g., "assets/textures/hudui.png")
    private val correctedFilepath: String = if (filepath.startsWith("/")) filepath.substring(1) else filepath

    var isLoaded = false
        private set

    /**
     * Loads the texture atlas from the specified filepath (as a classpath resource).
     * @throws RuntimeException if the texture cannot be loaded.
     */
    fun load() {
        MemoryStack.stackPush().use { stack ->
            // ... (STB image loading setup) ...
            val x = stack.mallocInt(1)
            val y = stack.mallocInt(1)
            val comp = stack.mallocInt(1)

            val imageBuffer: ByteBuffer = try {
                ResourceLoader.readResourceToByteBuffer(correctedFilepath)
            } catch (e: Exception) {
                throw RuntimeException("Resource load failed for '$correctedFilepath'.", e)
            }

            val image: ByteBuffer? = stbi_load_from_memory(imageBuffer, x, y, comp, 4)

            if (image == null) {
                throw RuntimeException("Failed to decode texture file '$correctedFilepath': ${stbi_failure_reason()}")
            }
            // ... (OpenGL texture creation and cleanup) ...
            textureWidth = x.get(0)
            textureHeight = y.get(0)

            cellsPerRow = textureWidth / cellSizePx
            cellsPerCol = textureHeight / cellSizePx

            textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)
            glBindTexture(GL_TEXTURE_2D, 0)

            stbi_image_free(image)
            isLoaded = true
        }
    }

    // ... rest of the TextureAtlas class (bind, unbind, getUV, cleanup, companion object) remains the same

    /**
     * Binds this texture atlas to GL_TEXTURE_2D for drawing.
     */
    fun bind() {
        if (isLoaded) {
            glBindTexture(GL_TEXTURE_2D, textureId)
        }
    }

    /**
     * Unbinds the current texture.
     */
    fun unbind() {
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    /**
     * Returns the UV coordinates for a texture at a given grid index.
     * @param index The 0-based index of the texture in the atlas (reading left-to-right, top-to-bottom).
     * @return A FloatArray of [u0, v0, u1, v1].
     */
    fun getUV(index: Int): FloatArray {
        if (!isLoaded || cellsPerRow == 0 || cellsPerCol == 0) {
            // Return placeholder UVs (e.g., solid color) if atlas not loaded or invalid
            return floatArrayOf(0f, 0f, 1f, 1f)
        }

        val col = index % cellsPerRow
        val row = index / cellsPerRow

        val uSize = 1.0f / cellsPerRow.toFloat()
        val vSize = 1.0f / cellsPerCol.toFloat()

        val u0 = col * uSize
        val v0 = row * vSize
        val u1 = u0 + uSize
        val v1 = v0 + vSize

        return floatArrayOf(u0, v0, u1, v1)
    }

    /**
     * Cleans up the OpenGL texture resource.
     */
    fun cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId)
            textureId = 0
            isLoaded = false
        }
    }

    companion object {
        /**
         * Creates a placeholder texture atlas if loading fails.
         */
        fun createPlaceholder(): TextureAtlas {
            val placeholder = TextureAtlas("") // Path doesn't matter for placeholder
            placeholder.isLoaded = true
            placeholder.textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, placeholder.textureId)

            val placeholderSize = 16 // 16x16 red square
            val buffer = ByteBuffer.allocateDirect(placeholderSize * placeholderSize * 4)
            for (i in 0 until placeholderSize * placeholderSize) {
                buffer.put(0xFF.toByte()) // R
                buffer.put(0x00.toByte()) // G
                buffer.put(0x00.toByte()) // B
                buffer.put(0xFF.toByte()) // A
            }
            buffer.flip()

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, placeholderSize, placeholderSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)

            glBindTexture(GL_TEXTURE_2D, 0)
            placeholder.textureWidth = placeholderSize
            placeholder.textureHeight = placeholderSize
            placeholder.cellsPerRow = 1
            placeholder.cellsPerCol = 1
            return placeholder
        }
    }
}