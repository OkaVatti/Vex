package dev.vex.client.render

import org.lwjgl.opengl.GL11.*
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer

// ==================================================================================
// PLACEHOLDER / ASSUMED CLASSES
// You must define these types in your project or update the code to use your actual types
// ----------------------------------------------------------------------------------

/** Represents a texture handle. Replace with your actual Texture class. */
data class Texture(val id: Int)

/** Handles loading textures. Replace with your actual TextureLoader logic. */
object TextureLoader {
    // Note: This path should match where you store your ASCII texture.
    fun loadTexture(path: String): Texture {
        // In a real application, this would call your STB Image loading and return a valid GL texture ID.
        // For now, we return 0, which will cause the fallback to print a box (if implemented).
        println("TextureLoader: Placeholder loaded for '$path'")
        return Texture(0)
    }
}

/** Represents a color vector. Replace with your actual Vec4 struct/class. */
data class Vec4(val r: Float, val g: Float, val b: Float, val a: Float)
// ==================================================================================


class FontRenderer {
    // --- Primary Bitmap Font (ASCII.PNG) Fields ---
    private var asciiTexture: Texture
    private val asciiCharWidth: Float = 8f
    private var isAsciiLoaded: Boolean

    // --- TrueType Fallback Fields ---
    private val truetypeFont: TrueTypeFont
    private val isTruetypeLoaded: Boolean

    // A reusable struct for getting rendering data from stb_truetype
    private val charQuad: STBTTAlignedQuad

    init {
        // 1. Load Primary ASCII Bitmap
        val asciiPath = "assets/textures/ascii.png"
        try {
            // Note: If TextureLoader.loadTexture fails, asciiTexture.id might be 0.
            asciiTexture = TextureLoader.loadTexture(asciiPath)
            isAsciiLoaded = asciiTexture.id != 0
            if (isAsciiLoaded) println("FontRenderer: Primary font loaded successfully.")
        } catch (e: Exception) {
            println("FontRenderer: Failed to load primary font '$asciiPath'. Reason: ${e.message}")
            asciiTexture = Texture(0)
            isAsciiLoaded = false
        }

        // 2. Load TrueType Fallback Font
        val ttfPath = "assets/fonts/MinecraftRegular.otf"
        val firstCodepoint = 32 // Space
        val lastCodepoint = 255 // Extended ASCII/Latin-1 Supplement
        val charCount = lastCodepoint - firstCodepoint + 1

        val tempFont: TrueTypeFont = try {
            TrueTypeFont(ttfPath, 16f, firstCodepoint, charCount)
        } catch (e: Exception) {
            println("FontRenderer: Failed to load TrueType fallback '$ttfPath'. Reason: ${e.message}")
            // Create a minimal fallback that won't crash
            object : TrueTypeFont(ttfPath, 16f, firstCodepoint, charCount) {
                override fun getCharQuad(c: Char, x: Float, y: Float, quad: STBTTAlignedQuad): Boolean = false
                override fun getTextureID(): Int = 0
                override fun cleanup() {}
            }
        }

        truetypeFont = tempFont
        isTruetypeLoaded = truetypeFont.getTextureID() != 0
        if (isTruetypeLoaded) println("FontRenderer: TrueType fallback loaded successfully.")

        // 3. Allocate reusable memory for rendering
        charQuad = STBTTAlignedQuad.malloc()
    }

    /**
     * Renders a string using the primary bitmap font with TrueType fallback.
     * @param text The string to render.
     * @param x The starting x position.
     * @param y The starting y position (usually the baseline).
     * @param color The color to render the text.
     */
    fun drawString(text: String, x: Float, y: Float, color: Vec4) {
        // --- OpenGL Setup (Needs to happen once per draw batch) ---
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // You would typically bind your shader here
        // shader.bind()
        // shader.setUniform("u_color", color)

        var currentX = x
        val initialY = y

        // Start batching here if you are using a modern VBO/IBO approach

        for (char in text) {
            val charCode = char.code

            // --- Primary Bitmap Logic (Handles standard ASCII range) ---
            if (isAsciiLoaded && charCode >= 32 && charCode <= 127) {

                // Bind the ASCII texture
                glBindTexture(GL_TEXTURE_2D, asciiTexture.id)

                // Calculate UVs based on the 16x16 grid (assuming 256x256 texture and 16x16 chars)
                val u = (charCode % 16) * asciiCharWidth / 256f
                val v = (charCode / 16) * asciiCharWidth / 256f
                val u1 = u + asciiCharWidth / 256f
                val v1 = v + asciiCharWidth / 256f

                // Render the ASCII character quad
                renderQuad(
                    currentX, initialY, currentX + asciiCharWidth, initialY + asciiCharWidth,
                    u, v, u1, v1,
                    color,
                    asciiTexture.id
                )

                currentX += asciiCharWidth // Fixed width advance

                // --- TrueType Fallback Logic ---
            } else if (isTruetypeLoaded) {
                // Character is outside the primary range or primary load failed, use TTF.

                // Bind the TrueType texture
                glBindTexture(GL_TEXTURE_2D, truetypeFont.getTextureID())

                // Get the quad data from the TrueType font atlas
                if (truetypeFont.getCharQuad(char, currentX, initialY, charQuad)) {

                    // Render the TrueType quad (Note: TTF quad coords are usually already correct for screen space)
                    renderQuad(
                        charQuad.x0(), charQuad.y0(), charQuad.x1(), charQuad.y1(),
                        charQuad.s0(), charQuad.t0(), charQuad.s1(), charQuad.t1(),
                        color,
                        truetypeFont.getTextureID()
                    )

                    // Advance the cursor to the position returned by STBTT (x1 position from the quad)
                    currentX = charQuad.x1()

                } else {
                    // TTF could not find the character either, advance cursor anyway
                    currentX += 8f
                }
            } else {
                // --- Complete Fallback (Draw a solid box or skip) ---
                glDisable(GL_TEXTURE_2D)
                glColor4f(color.r, color.g, color.b, color.a)
                glBegin(GL_QUADS)
                glVertex2f(currentX, initialY)
                glVertex2f(currentX, initialY + asciiCharWidth)
                glVertex2f(currentX + asciiCharWidth, initialY + asciiCharWidth)
                glVertex2f(currentX + asciiCharWidth, initialY)
                glEnd()
                currentX += asciiCharWidth
            }
        }

        // Finalize batching and draw here if using a modern renderer

        // --- OpenGL Cleanup ---
        // shader.unbind()
        glDisable(GL_BLEND)
        glDisable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    /**
     * Placeholder for actual rendering logic. This should be replaced with your
     * vertex buffer (VBO/IBO) or immediate mode rendering implementation.
     */
    private fun renderQuad(x0: Float, y0: Float, x1: Float, y1: Float, s0: Float, t0: Float, s1: Float, t1: Float, color: Vec4, textureId: Int) {
        // Simple Immediate Mode (for demonstration only; bad performance)
        glBindTexture(GL_TEXTURE_2D, textureId)
        glColor4f(color.r, color.g, color.g, color.a)

        glBegin(GL_QUADS)
        // Top-Left
        glTexCoord2f(s0, t0); glVertex2f(x0, y0)
        // Bottom-Left
        glTexCoord2f(s0, t1); glVertex2f(x0, y1)
        // Bottom-Right
        glTexCoord2f(s1, t1); glVertex2f(x1, y1)
        // Top-Right
        glTexCoord2f(s1, t0); glVertex2f(x1, y0)
        glEnd()
    }

    /**
     * Releases all managed native resources (texture memory, STB structs).
     */
    fun cleanup() {
        // Clean up TrueType resources (which handles its own texture)
        truetypeFont.cleanup()

        // Clean up reusable STB struct
        charQuad.free()

        // Clean up the ASCII texture
        if (asciiTexture.id != 0) {
            glDeleteTextures(asciiTexture.id)
            // Note: If TextureLoader is managed elsewhere, you might skip this line.
        }

        println("FontRenderer resources cleaned up.")
    }
}