package dev.vex.client.render

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryUtil
import org.joml.Matrix4f
import java.nio.FloatBuffer

/**
 * Renders UI elements using modern OpenGL (no fixed-function pipeline)
 */
class UIRenderer(private val width: Int, private val height: Int) {

    private var vao = 0
    private var vbo = 0
    private var uiShader: UIShader? = null

    init {
        setupUIRendering()
    }

    private fun setupUIRendering() {
        // Create VAO and VBO for UI quads
        vao = glGenVertexArrays()
        vbo = glGenBuffers()

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        // Allocate buffer (we'll update it per frame)
        glBufferData(GL_ARRAY_BUFFER, 1024 * 4, GL_DYNAMIC_DRAW)

        // Position attribute (2D)
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 16, 0)
        glEnableVertexAttribArray(0)

        // Color attribute
        glVertexAttribPointer(1, 4, GL_FLOAT, false, 16, 8)
        glEnableVertexAttribArray(1)

        glBindVertexArray(0)

        // Create simple UI shader
        uiShader = UIShader(width, height)
    }

    fun renderTitleScreen(menuItems: List<String>, selectedIndex: Int) {
        uiShader?.use()
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Dark background
        drawQuad(0f, 0f, width.toFloat(), height.toFloat(), 0.15f, 0.15f, 0.15f, 1f)

        // Title text (simplified - just boxes for now)
        val titleY = height * 0.25f
        drawQuad(width / 2f - 80f, titleY, 160f, 40f, 1f, 1f, 0.5f, 1f)

        // Menu items
        val startY = height * 0.5f
        val itemSpacing = 50f

        menuItems.forEachIndexed { index, _ ->
            val y = startY + index * itemSpacing
            val isSelected = index == selectedIndex

            if (isSelected) {
                drawQuad(width / 2f - 110f, y, 220f, 40f, 0.8f, 0.8f, 0.8f, 0.9f)
            } else {
                drawQuad(width / 2f - 110f, y, 220f, 40f, 0.3f, 0.3f, 0.3f, 0.7f)
            }
        }

        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun renderPauseMenu(menuItems: List<String>, selectedIndex: Int) {
        uiShader?.use()
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Dark overlay
        drawQuad(0f, 0f, width.toFloat(), height.toFloat(), 0f, 0f, 0f, 0.7f)

        // Pause title
        val titleY = height * 0.3f
        drawQuad(width / 2f - 70f, titleY, 140f, 35f, 1f, 1f, 0.5f, 1f)

        // Menu items
        val startY = height * 0.45f
        val itemSpacing = 50f

        menuItems.forEachIndexed { index, _ ->
            val y = startY + index * itemSpacing
            val isSelected = index == selectedIndex

            if (isSelected) {
                drawQuad(width / 2f - 110f, y, 220f, 40f, 0.8f, 0.8f, 0.8f, 0.9f)
            } else {
                drawQuad(width / 2f - 110f, y, 220f, 40f, 0.3f, 0.3f, 0.3f, 0.7f)
            }
        }

        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    fun renderHUD(fps: Int, position: org.joml.Vector3f) {
        uiShader?.use()
        glDisable(GL_DEPTH_TEST)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // FPS box
        drawQuad(5f, 5f, 100f, 20f, 0f, 0f, 0f, 0.5f)

        // Position box
        drawQuad(5f, 30f, 200f, 20f, 0f, 0f, 0f, 0.5f)

        // Crosshair
        val centerX = width / 2f
        val centerY = height / 2f
        val size = 2f
        val length = 10f

        // Horizontal
        drawQuad(centerX - length, centerY - size, length * 2, size * 2, 1f, 1f, 1f, 0.8f)
        // Vertical
        drawQuad(centerX - size, centerY - length, size * 2, length * 2, 1f, 1f, 1f, 0.8f)

        glDisable(GL_BLEND)
        glEnable(GL_DEPTH_TEST)
    }

    private fun drawQuad(x: Float, y: Float, width: Float, height: Float,
                         r: Float, g: Float, b: Float, a: Float) {
        val vertices = floatArrayOf(
            // Position (x, y), Color (r, g, b, a)
            x, y, r, g, b, a,
            x + width, y, r, g, b, a,
            x + width, y + height, r, g, b, a,

            x, y, r, g, b, a,
            x + width, y + height, r, g, b, a,
            x, y + height, r, g, b, a
        )

        glBindVertexArray(vao)
        glBindBuffer(GL_ARRAY_BUFFER, vbo)

        val buffer = MemoryUtil.memAllocFloat(vertices.size)
        buffer.put(vertices).flip()
        glBufferSubData(GL_ARRAY_BUFFER, 0, buffer)
        MemoryUtil.memFree(buffer)

        glDrawArrays(GL_TRIANGLES, 0, 6)
        glBindVertexArray(0)
    }

    fun cleanup() {
        if (vbo != 0) glDeleteBuffers(vbo)
        if (vao != 0) glDeleteVertexArrays(vao)
        uiShader?.cleanup()
    }
}

/**
 * Simple shader for UI rendering
 */
class UIShader(width: Int, height: Int) {
    private var programId = 0

    private val vertexShader = """
        #version 330 core
        layout (location = 0) in vec2 position;
        layout (location = 1) in vec4 color;
        
        out vec4 fragColor;
        
        uniform mat4 projection;
        
        void main() {
            gl_Position = projection * vec4(position, 0.0, 1.0);
            fragColor = color;
        }
    """.trimIndent()

    private val fragmentShader = """
        #version 330 core
        in vec4 fragColor;
        out vec4 outColor;
        
        void main() {
            outColor = fragColor;
        }
    """.trimIndent()

    init {
        programId = glCreateProgram()

        val vs = compileShader(vertexShader, GL_VERTEX_SHADER)
        val fs = compileShader(fragmentShader, GL_FRAGMENT_SHADER)

        glAttachShader(programId, vs)
        glAttachShader(programId, fs)
        glLinkProgram(programId)

        if (glGetProgrami(programId, GL_LINK_STATUS) == 0) {
            throw RuntimeException("Error linking UI shader: ${glGetProgramInfoLog(programId)}")
        }

        glDeleteShader(vs)
        glDeleteShader(fs)

        // Set orthographic projection
        use()
        val projLocation = glGetUniformLocation(programId, "projection")
        val projection = Matrix4f().ortho(0f, width.toFloat(), height.toFloat(), 0f, -1f, 1f)

        val buffer = MemoryUtil.memAllocFloat(16)
        projection.get(buffer)
        glUniformMatrix4fv(projLocation, false, buffer)
        MemoryUtil.memFree(buffer)
    }

    private fun compileShader(source: String, type: Int): Int {
        val shader = glCreateShader(type)
        glShaderSource(shader, source)
        glCompileShader(shader)

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            throw RuntimeException("Error compiling UI shader: ${glGetShaderInfoLog(shader)}")
        }

        return shader
    }

    fun use() {
        glUseProgram(programId)
    }

    fun cleanup() {
        if (programId != 0) glDeleteProgram(programId)
    }
}