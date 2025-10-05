package dev.vex.client.render

import dev.vex.client.util.ResourceLoader
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE
import org.lwjgl.stb.STBTTAlignedQuad
import org.lwjgl.stb.STBTTBakedChar
import org.lwjgl.stb.STBTruetype.stbtt_GetBakedQuad
import org.lwjgl.stb.STBTruetype.stbtt_BakeFontBitmap
import org.lwjgl.system.MemoryStack
import java.nio.ByteBuffer
import java.nio.FloatBuffer

/**
 * Loads and manages a TrueType Font using stb_truetype.h for creating a font atlas.
 * This class is designed to be the fallback renderer for characters outside the
 * primary bitmap font range.
 *
 * @property fontPath The file path to the TTF/OTF file.
 * @property pixelHeight The desired pixel height of the font (controls the scale of the glyphs).
 * @property firstCodepoint The first Unicode codepoint to include in the atlas (e.g., 32 for space).
 * @property charCount The number of consecutive characters to include starting from firstCodepoint.
 */
open class TrueTypeFont(
    private val fontPath: String,
    private val pixelHeight: Float,
    private val firstCodepoint: Int,
    private val charCount: Int
) {
    // --- Configuration ---
    private val ATLAS_WIDTH = 512 // Must be a power of two, adjust for character count/size
    private val ATLAS_HEIGHT = 512

    // --- STB & OpenGL Resources ---
    private var fontBuffer: ByteBuffer? = null // Holds the raw font data
    private var charData: STBTTBakedChar.Buffer? = null // Holds the baked glyph metrics
    private var textureID: Int = 0 // The OpenGL texture ID for the font atlas

    init {
        // Attempt to load and bake the font atlas
        try {
            loadAndBakeFont()
        } catch (e: Exception) {
            println("ERROR: Failed to initialize TrueTypeFont from '$fontPath'. ${e.message}")
            // Ensure resources are marked as null/0 if initialization failed
            charData = null
            textureID = 0
        }
    }

    /**
     * Loads the font file, bakes the glyphs into a bitmap, and uploads the bitmap
     * to an OpenGL texture atlas.
     *
     * @throws Exception if resource loading or baking fails.
     */
    private fun loadAndBakeFont() {
        // 1. Load the TTF file data into a ByteBuffer
        fontBuffer = ResourceLoader.loadResource(fontPath)

        if (fontBuffer == null) {
            throw Exception("Could not load font resource: $fontPath")
        }

        // 2. Prepare buffers for the atlas texture and baked character data
        val bitmap = BufferUtils.createByteBuffer(ATLAS_WIDTH * ATLAS_HEIGHT)
        charData = STBTTBakedChar.malloc(charCount)

        // 3. Bake the font characters into the bitmap and fill the charData buffer
        // ... (keep all your existing TrueTypeFont code exactly as you provided it)
// Only change this line in the loadAndBakeFont method:

// In the stbtt_BakeFontBitmap call, change from charData!! to charData?
        val bakedChars = stbtt_BakeFontBitmap(
            fontBuffer!!,
            pixelHeight,
            bitmap,
            ATLAS_WIDTH,
            ATLAS_HEIGHT,
            firstCodepoint,
            charData ?: throw Exception("Character data buffer is null")
        )

        // Check if baking was successful
        if (bakedChars <= 0) {
            throw Exception("stbtt_BakeFontBitmap failed (baked $bakedChars chars).")
        }

        // 4. Create and upload the OpenGL texture
        textureID = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureID)

        // Setup texture parameters
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        // Set clamping to edge to prevent artifacts
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)

        // Use GL_RED for modern OpenGL compatibility
        // The stb_truetype bitmap is 8-bit grayscale - sample the red channel in your shader
        glTexImage2D(
            GL_TEXTURE_2D,
            0,
            GL_RED, // Internal format: single channel (red)
            ATLAS_WIDTH,
            ATLAS_HEIGHT,
            0,
            GL_RED, // Source format: single channel (red)
            GL_UNSIGNED_BYTE,
            bitmap
        )

        // Unbind the texture
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    /**
     * Calculates the quad geometry and texture coordinates for a single character.
     * This function is the primary way FontRenderer.kt interacts with the baked font.
     *
     * @param c The character to render.
     * @param x The current X position (cursor position).
     * @param y The current Y position (cursor baseline).
     * @param quad The [STBTTAlignedQuad] structure to fill with the character's geometry.
     * @return true if the character was found and quad populated, false otherwise.
     */
    open fun getCharQuad(c: Char, x: Float, y: Float, quad: STBTTAlignedQuad): Boolean {
        // Safe check since charData is nullable
        val data = charData ?: return false
        if (textureID == 0) {
            return false // Font not loaded properly
        }

        val index = c.code - firstCodepoint
        if (index < 0 || index >= charCount) {
            return false // Character is outside the baked range
        }

        // Use a MemoryStack for temporary float buffers
        MemoryStack.stackPush().use { stack ->
            // Create FloatBuffers for xpos and ypos as required by LWJGL
            val xposBuffer = stack.floats(x)
            val yposBuffer = stack.floats(y)

            // Call the correct overload of stbtt_GetBakedQuad that accepts FloatBuffers
            stbtt_GetBakedQuad(
                data,          // STBTTBakedChar.Buffer
                ATLAS_WIDTH,   // Atlas width
                ATLAS_HEIGHT,  // Atlas height
                index,         // Character index
                xposBuffer,    // FloatBuffer for x position (will be advanced)
                yposBuffer,    // FloatBuffer for y position
                quad,          // Output quad
                true           // opengl_fillrule (use true for OpenGL)
            )

            return true
        }
    }

    /**
     * Get the OpenGL texture ID for the font atlas
     */
    open fun getTextureID(): Int {
        return textureID
    }

    /**
     * Releases all native resources associated with the font.
     * This should be called when the font is no longer needed.
     */
    open fun cleanup() {
        // Free the OpenGL Texture
        if (textureID != 0) {
            glDeleteTextures(textureID)
            textureID = 0
        }

        // Free the native memory allocated for the baked character data
        charData?.free()
        charData = null

        // Clear the font buffer reference
        fontBuffer = null

        println("TrueTypeFont resources cleaned up.")
    }
}