package dev.vex.client.render

import dev.vex.client.menu.MenuNavigator
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import kotlin.math.floor

class UIRenderer(private var width: Int, private var height: Int, private val navigator: MenuNavigator) {
    private val itemHeight = 48f
    private val itemSpacing = 12f
    private val menuWidthFraction = 0.5f
    private var hoverIndex: Int = -1
    private var font: FontRenderer? = null
    private var consoleOnly = false

    init {
        try {
            font = FontRenderer("SansSerif", java.awt.Font.PLAIN, 20)
        } catch (e: Throwable) {
            consoleOnly = true
            println("FontRenderer unavailable: ${e.message}. Falling back to console-only UI.")
        }
    }

    fun onResize(w: Int, h: Int) {
        this.width = w
        this.height = h
    }

    fun cleanup() {
        font?.cleanup()
    }

    fun onMouseMove(mx: Float, my: Float) {
        if (consoleOnly) return
        val items = navigator.getMenuItems()
        if (items.isEmpty()) {
            hoverIndex = -1
            return
        }
        val menuWidth = width * menuWidthFraction
        val menuLeft = (width - menuWidth) / 2f
        val totalHeight = items.size * itemHeight + (items.size - 1) * itemSpacing
        val menuTop = (height / 2f) - (totalHeight / 2f)

        if (mx < menuLeft || mx > menuLeft + menuWidth) {
            hoverIndex = -1
            return
        }
        val relativeY = my - menuTop
        if (relativeY < 0f || relativeY > totalHeight) {
            hoverIndex = -1
            return
        }
        val idx = floor(relativeY / (itemHeight + itemSpacing)).toInt().coerceAtLeast(0)
        hoverIndex = if (idx in items.indices) idx else -1
        if (hoverIndex >= 0) {
            navigator.setSelectedIndex(hoverIndex)
        }
    }

    fun onMouseClick(mx: Float, my: Float) {
        if (consoleOnly) {
            println("Menu click at: $mx,$my (console-only mode)")
            val items = navigator.getMenuItems()
            items.forEachIndexed { i, s -> println("$i: $s") }
            return
        }
        val items = navigator.getMenuItems()
        if (items.isEmpty()) return

        val menuWidth = width * menuWidthFraction
        val menuLeft = (width - menuWidth) / 2f
        val totalHeight = items.size * itemHeight + (items.size - 1) * itemSpacing
        val menuTop = (height / 2f) - (totalHeight / 2f)

        if (mx < menuLeft || mx > menuLeft + menuWidth) return

        val relativeY = my - menuTop
        if (relativeY < 0f || relativeY > totalHeight) return

        val index = floor(relativeY / (itemHeight + itemSpacing)).toInt().coerceAtLeast(0)
        navigator.setSelectedIndex(index)
        navigator.handleSelection()
    }

    fun renderTitleScreen(menuItems: List<String>, selectedIndex: Int) {
        if (consoleOnly) {
            println("=== TITLE SCREEN ===")
            menuItems.forEachIndexed { i, it ->
                val marker = if (i == selectedIndex) "->" else "  "
                println("$marker $it")
            }
            return
        }

        drawFullScreenQuad(0.12f, 0.12f, 0.12f)
        drawCenteredText("Vex - Beta 1.7.3 Recreation", height / 6f, 1.6f)

        renderMenu(menuItems, selectedIndex)
    }

    fun renderPauseMenu(menuItems: List<String>, selectedIndex: Int) {
        if (consoleOnly) {
            println("=== PAUSE MENU ===")
            menuItems.forEachIndexed { i, it ->
                val marker = if (i == selectedIndex) "->" else "  "
                println("$marker $it")
            }
            return
        }

        drawFullScreenQuad(0f, 0f, 0f, 0.6f)
        renderMenu(menuItems, selectedIndex)
    }

    fun renderHUD(fps: Int, playerPos: Vector3f) {
        if (consoleOnly) return
        val text = "FPS: $fps  Pos: ${playerPos.x.toInt()},${playerPos.y.toInt()},${playerPos.z.toInt()}"
        glDisable(GL_DEPTH_TEST)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        font?.drawText(8f, 18f, text, 1.0f)

        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glEnable(GL_DEPTH_TEST)
    }

    private fun renderMenu(items: List<String>, selectedIndex: Int) {
        val menuWidth = width * menuWidthFraction
        val menuLeft = (width - menuWidth) / 2f
        val totalHeight = items.size * itemHeight + (items.size - 1) * itemSpacing
        val menuTop = (height / 2f) - (totalHeight / 2f)

        for ((i, item) in items.withIndex()) {
            val top = menuTop + i * (itemHeight + itemSpacing)
            val bottom = top + itemHeight
            val left = menuLeft
            val right = menuLeft + menuWidth

            if (i == selectedIndex) {
                drawQuad(left, top, right, bottom, 0.25f, 0.55f, 0.9f, 1.0f)
            } else {
                drawQuad(left, top, right, bottom, 0.2f, 0.2f, 0.2f, 1.0f)
            }

            drawLineRect(left, top, right, bottom, 2f, 0f, 0f, 0f, 0.6f)

            val centerX = (left + right) / 2f
            val display = item

            font?.let { fr ->
                var approxWidth = 0f
                for (ch in display) {
                    val gi = fr.glyphInfos.getOrNull(ch.code)
                    approxWidth += (gi?.xAdvance ?: fr.fontSize) * 1.0f
                }
                val startX = centerX - approxWidth / 2f
                glDisable(GL_DEPTH_TEST)
                glMatrixMode(GL_PROJECTION)
                glPushMatrix()
                glLoadIdentity()
                glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
                glMatrixMode(GL_MODELVIEW)
                glPushMatrix()
                glLoadIdentity()

                fr.drawText(startX, top + itemHeight / 2f + 8f, display, 1.0f)

                glPopMatrix()
                glMatrixMode(GL_PROJECTION)
                glPopMatrix()
                glMatrixMode(GL_MODELVIEW)
                glEnable(GL_DEPTH_TEST)
            }
        }
    }

    private fun drawFullScreenQuad(r: Float, g: Float, b: Float, a: Float = 1.0f) {
        glDisable(GL_DEPTH_TEST)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        glColor4f(r, g, b, a)
        glBegin(GL_QUADS)
        glVertex2f(0f, 0f)
        glVertex2f(width.toFloat(), 0f)
        glVertex2f(width.toFloat(), height.toFloat())
        glVertex2f(0f, height.toFloat())
        glEnd()

        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glEnable(GL_DEPTH_TEST)
    }

    private fun drawQuad(left: Float, top: Float, right: Float, bottom: Float, r: Float, g: Float, b: Float, a: Float) {
        glDisable(GL_DEPTH_TEST)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        glColor4f(r, g, b, a)
        glBegin(GL_QUADS)
        glVertex2f(left, top)
        glVertex2f(right, top)
        glVertex2f(right, bottom)
        glVertex2f(left, bottom)
        glEnd()

        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glEnable(GL_DEPTH_TEST)
    }

    private fun drawLineRect(left: Float, top: Float, right: Float, bottom: Float, lineWidth: Float, r: Float, g: Float, b: Float, a: Float) {
        glDisable(GL_DEPTH_TEST)
        glLineWidth(lineWidth)
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()

        glColor4f(r, g, b, a)
        glBegin(GL_LINE_LOOP)
        glVertex2f(left, top)
        glVertex2f(right, top)
        glVertex2f(right, bottom)
        glVertex2f(left, bottom)
        glEnd()

        glPopMatrix()
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glEnable(GL_DEPTH_TEST)
    }

    private fun drawCenteredText(text: String, y: Float, scale: Float = 1.0f) {
        font?.let { fr ->
            var approxWidth = 0f
            for (ch in text) {
                val gi = fr.glyphInfos.getOrNull(ch.code)
                approxWidth += (gi?.xAdvance ?: fr.fontSize) * scale
            }
            val startX = (width / 2f) - approxWidth / 2f
            glDisable(GL_DEPTH_TEST)
            glMatrixMode(GL_PROJECTION)
            glPushMatrix()
            glLoadIdentity()
            glOrtho(0.0, width.toDouble(), height.toDouble(), 0.0, -1.0, 1.0)
            glMatrixMode(GL_MODELVIEW)
            glPushMatrix()
            glLoadIdentity()

            fr.drawText(startX, y, text, scale)

            glPopMatrix()
            glMatrixMode(GL_PROJECTION)
            glPopMatrix()
            glMatrixMode(GL_MODELVIEW)
            glEnable(GL_DEPTH_TEST)
        }
    }
}
