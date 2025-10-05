package dev.vex.client.render

import dev.vex.client.menu.MenuNavigator
import org.joml.Matrix4f
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import kotlin.math.floor

/**
 * Minimal UIRenderer that exposes methods used by VexGame:
 *  - constructor(width,height,menuNavigator,font)
 *  - onMouseMove, onMouseClick, onResize
 *  - renderHUD(fps, cameraPosition)
 *  - renderTitleScreen(menuItems, selectedIndex)
 *  - renderPauseMenu(...)
 *  - cleanup()
 *
 * This class intentionally uses immediate-mode GL drawing for simplicity.
 */
class UIRenderer(
    private var width: Int,
    private var height: Int,
    private val menuNavigator: MenuNavigator,
    private val font: FontRenderer
) {
    fun onMouseMove(x: Float, y: Float) {
        // Could update hover states — trivial for now
    }

    fun onMouseClick(x: Float, y: Float) {
        // Translate coordinates if needed and forward to menu system
    }

    fun onResize(w: Int, h: Int) {
        width = w
        height = h
    }

    /**
     * Render FPS and player coordinates at top-left of the screen.
     * VexGame calls: uiRenderer.renderHUD(fps, camera.position)
     */
    fun renderHUD(fps: Int, playerPos: Vector3f) {
        // Setup ortho
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        // Draw FPS
        val fpsText = "FPS: $fps"
        glColor3f(1f, 1f, 1f)
        font.drawText(8f, 8f, fpsText, 1.0f)

        // Draw player position
        val posText = "Pos: ${floor(playerPos.x).toInt()}, ${floor(playerPos.y).toInt()}, ${floor(playerPos.z).toInt()}"
        font.drawText(8f, 24f, posText, 1.0f)

        // Restore matrices
        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
    }

    fun renderTitleScreen(menuItems: List<String>, selectedIndex: Int) {
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        val title = "Vex - Beta 1.7.3 Recreation"
        glColor3f(1f, 1f, 1f)
        val x = (width / 2f) - (title.length * 4f)
        font.drawText(x, 60f, title, 1.0f)

        var menuY = 120f
        for ((i, item) in menuItems.withIndex()) {
            if (i == selectedIndex) glColor3f(1f, 1f, 0f) else glColor3f(1f, 1f, 1f)
            val mx = (width / 2f) - (item.length * 4f)
            font.drawText(mx, menuY, item, 1.0f)
            menuY += 18f
        }

        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
    }

    fun renderPauseMenu(menuItems: List<String>, selectedIndex: Int) {
        // Very similar to title screen layout
        renderTitleScreen(menuItems, selectedIndex)
    }

    fun cleanup() {
        // No VAOs/VBOs here; FontRenderer will be cleaned up elsewhere
    }
}
