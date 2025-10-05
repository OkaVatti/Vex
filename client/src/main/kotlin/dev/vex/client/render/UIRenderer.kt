package dev.vex.client.render

import dev.vex.client.menu.MenuNavigator
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import java.text.DecimalFormat

class UIRenderer(
    private var screenWidth: Int,
    private var screenHeight: Int,
    private val menuNavigator: MenuNavigator,
    private val font: FontRenderer
) {
    private val quad = HUDAxisAlignedQuad()
    private val hudAtlas: TextureAtlas
    private val decimalFormat = DecimalFormat("0.00")

    // Constants for HUD/UI elements within hudui.png (8x8 grid)
    private val hudCellSizePx = 8
    private val hudTextureScale = 2.0f
    private val inventoryHotbarIndex = 0
    private val heartFullIndex = 8
    private val heartHalfIndex = 9
    private val heartEmptyIndex = 10
    private val foodFullIndex = 11
    private val foodHalfIndex = 12
    private val foodEmptyIndex = 13
    private val crosshairIndex = 1

    // FIX: Adjusting paths to assume full classpath path if in source folder,
    // or standard resources path if moved to src/main/resources.
    // Assuming standard structure for now:
    // If textures are in 'src/main/resources/assets/textures/...' -> path is 'assets/textures/...'
    // If textures are in 'src/main/kotlin/dev/vex/client/assets/textures/...' -> path is 'dev/vex/client/assets/textures/...'
    // For now, we will use the assumed path, which is relative to the classpath root.
    private val FONT_PATH = "assets/textures/font/8x8_font.png"
    private val HUD_ATLAS_PATH = "assets/textures/hudui.png"


    init {
        // Initialize font with the corrected path
        // The FontRenderer constructor will handle any minor path stripping/correction.
        // Re-initialize font here if it's not passed in already initialized (assuming it is).

        // Load the HUD Atlas
        hudAtlas = TextureAtlas(HUD_ATLAS_PATH)
        try {
            hudAtlas.load()
            println("HUD/UI atlas loaded successfully.")
        } catch (e: Exception) {
            println("Warning: Could not load HUD/UI atlas - ${e.message}")
        }
    }

    // ... rest of the UIRenderer implementation remains the same

    fun onResize(width: Int, height: Int) {
        screenWidth = width
        screenHeight = height
    }

    fun renderHUD(fps: Int, playerPos: Vector3f) {
        setup2DProjection()
        glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        glColor4f(1f, 1f, 1f, 1f) // Set color to white for textured quads

        hudAtlas.bind()

        // Draw Crosshair
        val crosshairSize = hudCellSizePx * hudTextureScale
        drawHudElement(crosshairIndex, (screenWidth - crosshairSize) / 2f, (screenHeight - crosshairSize) / 2f, hudTextureScale)

        // Draw health and food bars
        val barY = screenHeight - (hudCellSizePx * hudTextureScale * 2.5f)

        val maxItemsPerBar = 10
        val elementWidth = hudCellSizePx * hudTextureScale
        val elementSpacing = elementWidth - 2f
        val gapWidth = 4f

        val totalElementsWidth = (maxItemsPerBar * 2 * elementSpacing) + gapWidth

        val centerBarAreaX = (screenWidth / 2f) - (totalElementsWidth / 2f)

        val heartStartX = centerBarAreaX
        val foodStartX = heartStartX + (maxItemsPerBar * elementSpacing) + gapWidth

        drawHearts(maxItemsPerBar, 8, barY, heartStartX)
        drawFood(maxItemsPerBar, 8, barY, foodStartX)

        hudAtlas.unbind()

        // Draw debug info text
        font.drawText(10f, 10f, "FPS: $fps", 2.0f)
        font.drawText(10f, 30f, "X: ${decimalFormat.format(playerPos.x)}", 2.0f)
        font.drawText(10f, 50f, "Y: ${decimalFormat.format(playerPos.y)}", 2.0f)
        font.drawText(10f, 70f, "Z: ${decimalFormat.format(playerPos.z)}", 2.0f)

        restore3DProjection()
    }

    fun renderTitleScreen(menuItems: List<String>, selectedIndex: Int) {
        setup2DProjection()
        val startY = screenHeight / 2f
        for ((index, item) in menuItems.withIndex()) {
            val textColor = if (index == selectedIndex) floatArrayOf(1.0f, 1.0f, 0.0f) else floatArrayOf(1.0f, 1.0f, 1.0f)
            glColor3f(textColor[0], textColor[1], textColor[2])

            val textWidth = item.length * font.glyphWidthPx * 2.0f
            font.drawText((screenWidth - textWidth) / 2f, startY + index * 30f, item, 2.0f)
        }

        restore3DProjection()
    }

    fun renderPauseMenu(menuItems: List<String>, selectedIndex: Int) {
        setup2DProjection()

        glDisable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glColor4f(0.0f, 0.0f, 0.0f, 0.5f)
        glBegin(GL_QUADS)
        glVertex2f(0f, 0f)
        glVertex2f(0f, screenHeight.toFloat())
        glVertex2f(screenWidth.toFloat(), screenHeight.toFloat())
        glVertex2f(screenWidth.toFloat(), 0f)
        glEnd()
        glEnable(GL_TEXTURE_2D)

        glColor3f(1.0f, 1.0f, 1.0f)
        val title = "Game Paused"
        val titleWidth = title.length * font.glyphWidthPx * 3.0f
        font.drawText((screenWidth - titleWidth) / 2f, screenHeight / 4f, title, 3.0f)

        val startY = screenHeight / 2f - (menuItems.size * 15f)
        for ((index, item) in menuItems.withIndex()) {
            val textColor = if (index == selectedIndex) floatArrayOf(1.0f, 1.0f, 0.0f) else floatArrayOf(1.0f, 1.0f, 1.0f)
            glColor3f(textColor[0], textColor[1], textColor[2])
            val textWidth = item.length * font.glyphWidthPx * 2.0f
            font.drawText((screenWidth - textWidth) / 2f, startY + index * 30f, item, 2.0f)
        }

        restore3DProjection()
    }

    private fun drawHudElement(index: Int, x: Float, y: Float, scale: Float) {
        if (!hudAtlas.isLoaded) return

        val (u0, v0, u1, v1) = getHudUV(index)
        val width = hudCellSizePx * scale
        val height = hudCellSizePx * scale

        quad.draw(x, y, width, height, u0, v0, u1, v1)
    }

    private fun getHudUV(index: Int): FloatArray {
        val cellsPerRow = 32

        val col = index % cellsPerRow
        val row = index / cellsPerRow

        val uSize = 1.0f / cellsPerRow.toFloat()
        val vSize = 1.0f / cellsPerRow.toFloat()

        val u0 = col * uSize
        val v0 = row * vSize
        val u1 = u0 + uSize
        val v1 = v0 + vSize
        return floatArrayOf(u0, v0, u1, v1)
    }

    private fun drawHearts(maxHearts: Int, currentHearts: Int, y: Float, startX: Float) {
        val elementWidth = hudCellSizePx * hudTextureScale
        val elementSpacing = elementWidth - 2f
        for (i in 0 until maxHearts) {
            val x = startX + i * elementSpacing
            when {
                currentHearts >= i + 1 -> drawHudElement(heartFullIndex, x, y, hudTextureScale)
                currentHearts > i -> drawHudElement(heartHalfIndex, x, y, hudTextureScale)
                else -> drawHudElement(heartEmptyIndex, x, y, hudTextureScale)
            }
        }
    }

    private fun drawFood(maxFood: Int, currentFood: Int, y: Float, startX: Float) {
        val elementWidth = hudCellSizePx * hudTextureScale
        val elementSpacing = elementWidth - 2f
        for (i in 0 until maxFood) {
            val x = startX + i * elementSpacing
            when {
                currentFood >= i + 1 -> drawHudElement(foodFullIndex, x, y, hudTextureScale)
                currentFood > i -> drawHudElement(foodHalfIndex, x, y, hudTextureScale)
                else -> drawHudElement(foodEmptyIndex, x, y, hudTextureScale)
            }
        }
    }

    private fun setup2DProjection() {
        glMatrixMode(GL_PROJECTION)
        glPushMatrix()
        glLoadIdentity()
        glOrtho(0.0, screenWidth.toDouble(), screenHeight.toDouble(), 0.0, -1.0, 1.0)
        glMatrixMode(GL_MODELVIEW)
        glPushMatrix()
        glLoadIdentity()
    }

    private fun restore3DProjection() {
        glMatrixMode(GL_PROJECTION)
        glPopMatrix()
        glMatrixMode(GL_MODELVIEW)
        glPopMatrix()
    }

    fun cleanup() {
        if (hudAtlas.isLoaded) {
            hudAtlas.cleanup()
        }
    }
}