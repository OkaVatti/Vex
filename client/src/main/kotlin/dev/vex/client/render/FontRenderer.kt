package dev.vex.client.render

import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.IntBuffer

/**
 * Simple FontRenderer which tries to load an ASCII font atlas image (expected layout: 16x16 grid,
 * 256 glyphs). If loading fails it falls back to drawing plain quads for each character so UI text
 * remains visible.
 *
 * drawText(x,y,text,scale) draws text using immediate-mode textured quads (or fallback quads).
 */
class FontRenderer(private val filepath: String) {
    private var textureId = 0
    var isLoaded = false
        private set

    // assumed glyph grid
    private val glyphsPerRow = 16
    private val glyphWidthPx = 8
    private val glyphHeightPx = 8

    init {
        try {
            loadTexture(filepath)
            isLoaded = true
            println("FontRenderer: loaded '$filepath' as GL texture #$textureId")
        } catch (e: Exception) {
            isLoaded = false
            println("FontRenderer: failed to load '$filepath' - using placeholder. Reason: ${e.message}")
        }
    }

    private fun loadTexture(path: String) {
        MemoryStack.stackPush().use { stack ->
            val x = stack.mallocInt(1)
            val y = stack.mallocInt(1)
            val comp = stack.mallocInt(1)

            // stbi_load expects a forward slash or platform path; allow either
            val image: ByteBuffer? = stbi_load(path.replace('/', java.io.File.separatorChar), x, y, comp, 4)
                ?: stbi_load(path, x, y, comp, 4)

            if (image == null) {
                throw RuntimeException("Failed to read texture file '$path': ${stbi_failure_reason()}")
            }

            val width = x.get(0)
            val height = y.get(0)

            textureId = glGenTextures()
            glBindTexture(GL_TEXTURE_2D, textureId)
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1)

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)

            // Upload (the image buffer is RGBA)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, image)
            glBindTexture(GL_TEXTURE_2D, 0)

            stbi_image_free(image)
        }
    }

    /**
     * Draw a string at pixel-space coordinates (top-left origin) using immediate-mode GL.
     * scale is a multiplier for glyph pixel size.
     */
    fun drawText(x: Float, y: Float, text: String, scale: Float = 1.0f) {
        if (isLoaded && textureId != 0) {
            glEnable(GL_TEXTURE_2D)
            glBindTexture(GL_TEXTURE_2D, textureId)
        } else {
            // no texture; make sure texturing is disabled for fallback rectangles
            glDisable(GL_TEXTURE_2D)
        }

        glPushMatrix()
        // immediate-mode uses current color; caller usually sets it
        var penX = x
        val penY = y

        // If using atlas, compute UV per glyph based on 16x16 grid
        val atlasCellW = 1.0f / glyphsPerRow.toFloat()
        val atlasCellH = 1.0f / glyphsPerRow.toFloat()

        for (ch in text) {
            val code = ch.code and 0xFF
            val gx = (code % glyphsPerRow)
            val gy = (code / glyphsPerRow)

            val px = penX
            val py = penY
            val w = glyphWidthPx * scale
            val h = glyphHeightPx * scale

            if (isLoaded && textureId != 0) {
                val u0 = gx * atlasCellW
                val v0 = gy * atlasCellH
                val u1 = u0 + atlasCellW
                val v1 = v0 + atlasCellH

                glBegin(GL_QUADS)
                glTexCoord2f(u0, v0); glVertex2f(px, py)
                glTexCoord2f(u0, v1); glVertex2f(px, py + h)
                glTexCoord2f(u1, v1); glVertex2f(px + w, py + h)
                glTexCoord2f(u1, v0); glVertex2f(px + w, py)
                glEnd()
            } else {
                // fallback: draw a visible rectangle per glyph (so menu text is visible)
                glBegin(GL_QUADS)
                glVertex2f(px, py)
                glVertex2f(px, py + h)
                glVertex2f(px + w, py + h)
                glVertex2f(px + w, py)
                glEnd()
            }

            penX += (glyphWidthPx + 1) * scale
        }

        glPopMatrix()

        if (isLoaded && textureId != 0) {
            glBindTexture(GL_TEXTURE_2D, 0)
            glDisable(GL_TEXTURE_2D)
        }
    }

    fun cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId)
            textureId = 0
        }
    }
}
