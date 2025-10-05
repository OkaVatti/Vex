package dev.vex.client.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13.*
import org.lwjgl.opengl.GL30.glGenerateMipmap
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.nio.file.Files
import java.nio.file.Paths

/**
 * Simple texture atlas loader using STBImage + OpenGL.
 *
 * Usage supported by the rest of the code:
 *  - TextureAtlas("path/to/atlas.png").load()
 *  - TextureAtlas.createPlaceholder()
 *  - atlas.bind()
 *  - atlas.cleanup()
 */
class TextureAtlas(
    private val filepath: String? = null,
    val gridCols: Int = 16,
    val gridRows: Int = 16
) {
    var textureId: Int = 0
        private set
    var width: Int = 0
        private set
    var height: Int = 0
        private set
    var channels: Int = 0
        private set

    /**
     * Load the texture from the filepath provided in the constructor.
     * Must be called after an OpenGL context is created.
     */
    fun load() {
        val path = filepath ?: throw IllegalArgumentException("No filepath provided to TextureAtlas.load()")
        val bytes = try {
            Files.readAllBytes(Paths.get(path))
        } catch (ex: Exception) {
            throw RuntimeException("Failed to read texture file '$path': ${ex.message}", ex)
        }

        val buffer = MemoryUtil.memAlloc(bytes.size)
        buffer.put(bytes)
        buffer.flip()

        STBImage.stbi_set_flip_vertically_on_load(true)
        MemoryStack.stackPush().use { stack ->
            val w: IntBuffer = stack.mallocInt(1)
            val h: IntBuffer = stack.mallocInt(1)
            val ch: IntBuffer = stack.mallocInt(1)

            val image: ByteBuffer? = STBImage.stbi_load_from_memory(buffer, w, h, ch, 0)
            MemoryUtil.memFree(buffer)

            if (image == null) {
                val reason = STBImage.stbi_failure_reason()
                throw RuntimeException("Failed to load image '$path' with STB: $reason")
            }

            width = w.get(0)
            height = h.get(0)
            channels = ch.get(0)

            // Create GL texture
            textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)

            // Filtering and wrap
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST_MIPMAP_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

            // Upload pixel data
            val format = when (channels) {
                3 -> GL_RGB
                4 -> GL_RGBA
                else -> GL_RGBA
            }

            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
            glTexImage2D(GL_TEXTURE_2D, 0, format, width, height, 0, format, GL_UNSIGNED_BYTE, image)
            glGenerateMipmap(GL_TEXTURE_2D)

            STBImage.stbi_image_free(image)
            glBindTexture(GL_TEXTURE_2D, 0)
        }
    }

    /** Bind to texture unit 0 by default */
    fun bind() = bind(0)

    /** Bind to a specified texture unit */
    fun bind(unit: Int) {
        // Query supported units and clamp requested unit to valid range
        val maxUnits = glGetInteger(GL_MAX_TEXTURE_UNITS)
        val unitToUse = when {
            unit < 0 -> 0
            unit >= maxUnits -> 0
            else -> unit
        }
        glActiveTexture(GL_TEXTURE0 + unitToUse)
        glBindTexture(GL_TEXTURE_2D, textureId)
    }

    /** Delete GL texture */
    fun cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId)
            textureId = 0
        }
    }

    companion object {
        /**
         * Create a minimal placeholder atlas (1x1 pixel). Useful when loading fails.
         */
        fun createPlaceholder(): TextureAtlas {
            val atlas = TextureAtlas(null)
            atlas.textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, atlas.textureId)

            // 1x1 black pixel RGBA
            val px = MemoryUtil.memAlloc(4)
            px.put(0.toByte()).put(0.toByte()).put(0.toByte()).put(255.toByte())
            px.flip()

            atlas.width = 1
            atlas.height = 1
            atlas.channels = 4

            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, 1, 1, 0, GL_RGBA, GL_UNSIGNED_BYTE, px)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glBindTexture(GL_TEXTURE_2D, 0)

            MemoryUtil.memFree(px)
            return atlas
        }
    }
}
