package dev.vex.client.render

import org.lwjgl.opengl.GL11.*

/**
 * Utility class to draw a simple 2D textured quad using immediate mode.
 * Used by UIRenderer for HUD elements.
 */
class HUDAxisAlignedQuad {
    fun draw(x: Float, y: Float, width: Float, height: Float, u0: Float, v0: Float, u1: Float, v1: Float) {
        glBegin(GL_QUADS)

        // Top-Left
        glTexCoord2f(u0, v0);
        glVertex2f(x, y)

        // Bottom-Left
        glTexCoord2f(u0, v1);
        glVertex2f(x, y + height)

        // Bottom-Right
        glTexCoord2f(u1, v1);
        glVertex2f(x + width, y + height)

        // Top-Right
        glTexCoord2f(u1, v0);
        glVertex2f(x + width, y)

        glEnd()
    }
}