package dev.vex.client.render

import org.lwjgl.opengl.GL11
import org.lwjgl.stb.STBImage
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

/**
 * Manages an OpenGL texture atlas.
 * Handles loading from file and creating a placeholder if loading fails.
 */
class TextureAtlas(private val filepath: String) {
    private var textureId: Int = 0

    fun load() {
        MemoryStack.stackPush().use { stack ->
            val w = stack.mallocInt(1)
            val h = stack.mallocInt(1)
            val channels = stack.mallocInt(1)

            // Load image data
            STBImage.stbi_set_flip_vertically_on_load(true)
            val imageBuffer = STBImage.stbi_load(filepath, w, h, channels, 4)
                ?: throw RuntimeException("Failed to load texture: $filepath\n${STBImage.stbi_failure_reason()}")

            // Create OpenGL texture
            textureId = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)

            // Set texture parameters for pixelated style
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            // Upload texture data to GPU
            GL11.glTexImage2D(
                GL11.GL_TEXTURE_2D,
                0,
                GL11.GL_RGBA,
                w.get(0),
                h.get(0),
                0,
                GL11.GL_RGBA,
                GL11.GL_UNSIGNED_BYTE,
                imageBuffer
            )

            // Free image data from memory
            STBImage.stbi_image_free(imageBuffer)
        }
    }

    fun bind() {
        if (textureId != 0) {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId)
        }
    }

    fun cleanup() {
        if (textureId != 0) {
            GL11.glDeleteTextures(textureId)
        }
    }

    companion object {
        /**
         * Creates a fallback placeholder texture for when the main atlas fails to load.
         * This generates a 2x2 magenta and black checkerboard pattern.
         */
        fun createPlaceholder(): TextureAtlas {
            val atlas = TextureAtlas("placeholder")
            atlas.textureId = GL11.glGenTextures()
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, atlas.textureId)

            // Set texture parameters for pixel art
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            // Create a 2x2 pink/black checkerboard texture buffer
            val pixels = ByteBuffer.allocateDirect(2 * 2 * 4) // 2x2 pixels, 4 bytes per pixel (RGBA)
            val magenta = -65281 // 0xFFFF00FF in integer representation
            val black = -16777216   // 0xFF000000 in integer representation
            pixels.asIntBuffer().put(intArrayOf(magenta, black, black, magenta)).flip()

            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, 2, 2, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels)

            // Unbind texture
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0)

            println("Created placeholder texture with ID: ${atlas.textureId}")
            return atlas
        }
    }
}