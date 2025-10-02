package dev.vex.client

import dev.vex.client.render.*
import dev.vex.client.world.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.joml.Matrix4f

/**
 * Main game class - Beta 1.7.3 style Minecraft clone
 */
class VexGame {
    private var window: Long = 0
    private var width = 1280
    private var height = 720

    // Core systems
    private lateinit var camera: Camera
    private lateinit var world: World
    private lateinit var blockAtlas: TextureAtlas
    private lateinit var shader: ShaderProgram
    private lateinit var stateManager: GameStateManager
    private lateinit var uiRenderer: UIRenderer

    // Timing
    private var lastFrameTime = 0.0
    private var deltaTime = 0.0f
    private var fps = 0
    private var fpsCounter = 0
    private var fpsTimer = 0.0

    // Input state
    private val keys = BooleanArray(GLFW_KEY_LAST)
    private var firstMouse = true
    private var lastX = width / 2.0
    private var lastY = height / 2.0

    fun run() {
        init()
        gameLoop()
        cleanup()
    }

    private fun init() {
        // Initialize GLFW
        GLFWErrorCallback.createPrint(System.err).set()

        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        // OpenGL 3.3 Core Profile
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3)
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE)
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)

        window = glfwCreateWindow(width, height, "Vex - Beta 1.7.3 Recreation", 0, 0)
        if (window == 0L) {
            throw RuntimeException("Failed to create GLFW window")
        }

        setupCallbacks()

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1) // V-Sync
        glfwShowWindow(window)

        GL.createCapabilities()

        // OpenGL state
        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        // Initialize game systems
        camera = Camera()
        world = World()
        stateManager = GameStateManager()
        uiRenderer = UIRenderer(width, height)

        // Load texture atlas
        blockAtlas = TextureAtlas("assets/textures/blocks.png")
        try {
            blockAtlas.load()
            println("Block atlas loaded successfully")
        } catch (e: Exception) {
            println("Warning: Could not load block atlas - ${e.message}")
            println("Creating placeholder textures...")
            createPlaceholderAtlas()
        }

        // Create shader
        shader = ShaderProgram()
        shader.create(ShaderProgram.VERTEX_SHADER, ShaderProgram.FRAGMENT_SHADER)
        shader.createUniform("projectionMatrix")
        shader.createUniform("viewMatrix")
        shader.createUniform("modelMatrix")
        shader.createUniform("textureSampler")
        shader.createUniform("skyColor")
        shader.createUniform("sunPosition")
        shader.createUniform("ambientStrength")
        shader.createUniform("fogDensity")
        shader.createUniform("fogGradient")

        // Generate initial chunks
        println("Generating world...")
        for (x in -4..4) {
            for (z in -4..4) {
                world.loadChunk(x, z)
            }
        }

        println("Vex initialized successfully!")
        println("Controls:")
        println("  WASD - Move")
        println("  Space - Jump")
        println("  Shift - Sprint")
        println("  Ctrl - Sneak")
        println("  ESC - Pause/Menu")
    }

    private fun createPlaceholderAtlas() {
        // Create simple colored texture atlas programmatically
        // This is a fallback if the PNG file isn't available
        blockAtlas = TextureAtlas.createPlaceholder()
    }

    private fun setupCallbacks() {
        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            if (key in 0 until GLFW_KEY_LAST) {
                keys[key] = action != GLFW_RELEASE
            }

            // Handle state-specific input
            if (stateManager.handleInput(key, action)) {
                // State manager handled this input
                if (stateManager.isPlaying()) {
                    // Entering game - capture mouse
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
                    firstMouse = true
                } else {
                    // Leaving game - release mouse
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                }
            }
        }

        glfwSetCursorPosCallback(window) { _, xpos, ypos ->
            if (!stateManager.isPlaying()) return@glfwSetCursorPosCallback

            if (firstMouse) {
                lastX = xpos
                lastY = ypos
                firstMouse = false
            }

            val xoffset = (xpos - lastX).toFloat()
            val yoffset = (lastY - ypos).toFloat()

            lastX = xpos
            lastY = ypos

            camera.processMouseMovement(xoffset, yoffset)
        }

        glfwSetFramebufferSizeCallback(window) { _, w, h ->
            width = w
            height = h
            glViewport(0, 0, w, h)
        }
    }

    private fun gameLoop() {
        lastFrameTime = glfwGetTime()

        while (!glfwWindowShouldClose(window)) {
            val currentTime = glfwGetTime()
            deltaTime = (currentTime - lastFrameTime).toFloat()
            lastFrameTime = currentTime

            // Update FPS
            fpsCounter++
            fpsTimer += deltaTime
            if (fpsTimer >= 1.0) {
                fps = fpsCounter
                fpsCounter = 0
                fpsTimer = 0.0
            }

            when {
                stateManager.isPlaying() -> {
                    processGameInput()
                    updateGame()
                    renderGame()
                }
                stateManager.isTitleScreen() -> {
                    renderTitleScreen()
                }
                stateManager.isPaused() -> {
                    renderGame() // Render game in background
                    renderPauseMenu()
                }
            }

            glfwSwapBuffers(window)
            glfwPollEvents()
        }
    }

    private fun processGameInput() {
        val forward = keys[GLFW_KEY_W]
        val backward = keys[GLFW_KEY_S]
        val left = keys[GLFW_KEY_A]
        val right = keys[GLFW_KEY_D]
        val jump = keys[GLFW_KEY_SPACE]
        val sprint = keys[GLFW_KEY_LEFT_SHIFT]
        val sneak = keys[GLFW_KEY_LEFT_CONTROL]

        camera.processMovement(forward, backward, left, right, jump, sprint, sneak, deltaTime)
    }

    private fun updateGame() {
        world.update(camera)
        world.generateMeshes(blockAtlas)
    }

    private fun renderGame() {
        // Clear screen
        val skyColor = getSkyColor()
        glClearColor(skyColor[0], skyColor[1], skyColor[2], 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shader.bind()

        // Set uniforms
        val aspectRatio = width.toFloat() / height.toFloat()
        shader.setUniform("projectionMatrix", camera.getProjectionMatrix(aspectRatio))
        shader.setUniform("viewMatrix", camera.getViewMatrix())
        shader.setUniform("modelMatrix", Matrix4f())
        shader.setUniform("textureSampler", 0)
        shader.setUniform("skyColor", skyColor[0], skyColor[1], skyColor[2])
        shader.setUniform("sunPosition", 1000f, 1000f, 1000f)
        shader.setUniform("ambientStrength", 0.4f)
        shader.setUniform("fogDensity", 0.007f)
        shader.setUniform("fogGradient", 1.5f)

        // Bind texture atlas
        blockAtlas.bind()

        // Render world
        world.render(camera)

        shader.unbind()

        // Render HUD (only if playing)
        if (stateManager.isPlaying()) {
            uiRenderer.renderHUD(fps, camera.position)
        }
    }

    private fun renderTitleScreen() {
        glClearColor(0.15f, 0.15f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        uiRenderer.renderTitleScreen(
            stateManager.getMenuItems(),
            stateManager.getSelectedMenuItem()
        )
    }

    private fun renderPauseMenu() {
        uiRenderer.renderPauseMenu(
            stateManager.getMenuItems(),
            stateManager.getSelectedMenuItem()
        )
    }

    private fun getSkyColor(): FloatArray {
        // Beta 1.7.3 sky color
        return floatArrayOf(0.53f, 0.81f, 0.98f)
    }

    private fun cleanup() {
        shader.cleanup()
        blockAtlas.cleanup()
        world.cleanup()
        uiRenderer.cleanup()

        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }
}

fun main() {
    try {
        VexGame().run()
    } catch (e: Exception) {
        e.printStackTrace()
        System.exit(-1)
    }
}