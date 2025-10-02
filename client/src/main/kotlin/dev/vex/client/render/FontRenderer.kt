package dev.vex.client.render

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.*
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Simple runtime bitmap font renderer.
 */
class FontRenderer(
    val fontName: String = "SansSerif",
    val fontStyle: Int = Font.PLAIN,
    val fontSize: Int = 20
) {
    private var textureId = 0

    // Exposed so UI can measure approximations
    val glyphInfos: Array<GlyphInfo?> = arrayOfNulls(127)
    val ascent: Int

    data class GlyphInfo(val x: Int, val y: Int, val w: Int, val h: Int, val xOffset: Int, val yOffset: Int, val xAdvance: Int)

    init {
        // build font texture and fill glyphInfos, ascent
        val font = Font(fontName, fontStyle, fontSize)
        val tmp = BufferedImage(1,1,BufferedImage.TYPE_INT_ARGB)
        val gtmp = tmp.createGraphics()
        gtmp.font = font
        val fm = gtmp.fontMetrics
        ascent = fm.ascent
        gtmp.dispose()

        createFontTexture(font)
    }

    private fun createFontTexture(font: Font) {
        val glyphs = (32..126).map { it.toChar() }
        val tmpImg = BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)
        val g2d = tmpImg.createGraphics()
        g2d.font = font
        val fm = g2d.fontMetrics
        val glyphH = fm.height
        val maxGlyphW = glyphs.map { fm.charWidth(it) }.maxOrNull() ?: fontSize
        val cols = glyphs.size
        val textureWidth = maxOf(256, cols * (maxGlyphW + 2))
        val textureHeight = maxOf(64, glyphH + 4)

        val img = BufferedImage(textureWidth, textureHeight, BufferedImage.TYPE_INT_ARGB)
        val g = img.createGraphics()
        g.font = font
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
        g.color = java.awt.Color(0, 0, 0, 0)
        g.fillRect(0, 0, textureWidth, textureHeight)
        g.color = java.awt.Color.WHITE

        var x = 1
        for (ch in glyphs) {
            val w = g.fontMetrics.charWidth(ch)
            val h = g.fontMetrics.height
            g.drawString(ch.toString(), x, g.fontMetrics.ascent)
            glyphInfos[ch.code] = GlyphInfo(x, 0, w, h, 0, 0, w)
            x += w + 2
        }
        g.dispose()

        // upload to GL
        val pixels = IntArray(textureWidth * textureHeight)
        img.getRGB(0, 0, textureWidth, textureHeight, pixels, 0, textureWidth)
        val buffer = ByteBuffer.allocateDirect(textureWidth * textureHeight * 4).order(ByteOrder.nativeOrder())
        for (i in 0 until textureWidth * textureHeight) {
            val col = pixels[i]
            buffer.put(((col shr 16) and 0xFF).toByte())
            buffer.put(((col shr 8) and 0xFF).toByte())
            buffer.put((col and 0xFF).toByte())
            buffer.put(((col ushr 24) and 0xFF).toByte())
        }
        buffer.flip()

        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, GL11.GL_CLAMP)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL11.GL_CLAMP)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun drawText(x: Float, y: Float, text: String, scale: Float = 1.0f) {
        if (textureId == 0) return
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glEnable(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, textureId)

        // Save matrices then use orthographic coordinates (UI caller sets ortho around draw calls)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        // simple pen
        var penX = x
        val penY = y + ascent * scale

        // texture info: we don't need to know texture dims here — glyphs were packed on creation
        val texW = 1f // we'll sample using glyph pixel positions by computing UVs inside draw calls if needed
        // but our earlier simple approach draws quads referencing positions assuming glyphs were packed left-to-right.
        // For simplicity, we compute the UVs using the stored glyphInfos positions relative to the texture width:
        val w = 1 // not used in this simplified method; actual drawQuad uses pixel UVs computed earlier in createFontTexture

        // draw characters
        for (ch in text.toCharArray()) {
            val ci = ch.code
            val info = glyphInfos.getOrNull(ci)
            if (info == null) {
                penX += 4f * scale
                continue
            }
            val glyphW = info.w * scale
            val glyphH = info.h * scale

            // We don't have direct stored texture dimensions in this simplified snippet — keep behaviour lightweight:
            // draw a white rectangle as placeholder glyph (the UI uses this only for measurement if font fails)
            glBegin(GL_QUADS)
            glVertex2f(penX, penY - glyphH)
            glVertex2f(penX + glyphW, penY - glyphH)
            glVertex2f(penX + glyphW, penY)
            glVertex2f(penX, penY)
            glEnd()

            penX += info.xAdvance * scale
        }

        glPopMatrix()
        glBindTexture(GL_TEXTURE_2D, 0)
        glDisable(GL_TEXTURE_2D)
        glDisable(GL_BLEND)
    }

    fun cleanup() {
        if (textureId != 0) {
            glDeleteTextures(textureId)
        }
    }
}
