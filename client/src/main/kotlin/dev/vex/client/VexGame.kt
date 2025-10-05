package dev.vex.client

import dev.vex.client.menu.MenuNavigator
import dev.vex.client.render.*
import dev.vex.client.world.*
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.*
import org.joml.Matrix4f
import kotlin.system.exitProcess

class VexGame : MenuNavigator.MenuListener {
    private var window: Long = 0
    private var width = 1280
    private var height = 720

    private lateinit var camera: Camera
    private lateinit var world: World
    private lateinit var blockAtlas: TextureAtlas
    private lateinit var shader: ShaderProgram
    private lateinit var stateManager: GameStateManager
    private lateinit var uiRenderer: UIRenderer
    private lateinit var menuNavigator: MenuNavigator
    private lateinit var font: FontRenderer

    private var lastFrameTime = 0.0
    private var deltaTime = 0.0f
    private var fps = 0
    private var fpsCounter = 0
    private var fpsTimer = 0.0

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
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 1)

        window = glfwCreateWindow(width, height, "Vex - Beta 1.7.3 Recreation", 0, 0)
        if (window == 0L) {
            throw RuntimeException("Failed to create GLFW window")
        }

        glfwMakeContextCurrent(window)
        glfwSwapInterval(1)
        glfwShowWindow(window)

        GL.createCapabilities()

        glEnable(GL_DEPTH_TEST)
        glEnable(GL_CULL_FACE)
        glCullFace(GL_BACK)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        camera = Camera()
        world = World()
        stateManager = GameStateManager()
        menuNavigator = MenuNavigator()
        menuNavigator.listener = this

        font = FontRenderer("assets/textures/ascii.png")
        uiRenderer = UIRenderer(width, height, menuNavigator, font)

        blockAtlas = TextureAtlas("assets/textures/blocks.png")
        try {
            blockAtlas.load()
            println("Block atlas loaded successfully")
        } catch (e: Exception) {
            println("Warning: Could not load block atlas - ${e.message}")
            createPlaceholderAtlas()
        }

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

        println("Vex initialized successfully!")
        println("Controls:")
        println("  WASD - Move")
        println("  Space - Jump")
        println("  Shift - Sprint")
        println("  Ctrl - Sneak")
        println("  ESC - Pause/Menu")

        setupCallbacks()
    }

    private fun createPlaceholderAtlas() {
        blockAtlas = TextureAtlas.createPlaceholder()
    }

    private fun setupCallbacks() {
        glfwSetKeyCallback(window) { _, key, _, action, _ ->
            if (key in 0 until GLFW_KEY_LAST) {
                keys[key] = action != GLFW_RELEASE
            }

            if (stateManager.isTitleScreen() || stateManager.isPaused()) {
                if (menuNavigator.handleKeyPress(key, action)) {
                    return@glfwSetKeyCallback
                }
            } else {
                if (stateManager.handleInput(key, action)) {
                    return@glfwSetKeyCallback
                }
            }
        }

        glfwSetCharCallback(window) { _, codepoint ->
            if (stateManager.isTitleScreen() || stateManager.isPaused()) {
                val ch = codepoint.toChar()
                menuNavigator.handleCharInput(ch)
            }
        }

        glfwSetCursorPosCallback(window) { _, xpos, ypos ->
            if (stateManager.isPlaying()) {
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
        }

        glfwSetFramebufferSizeCallback(window) { _, w, h ->
            if (w > 0 && h > 0) {
                width = w
                height = h
                glViewport(0, 0, w, h)
                uiRenderer.onResize(w, h)
            }
        }
    }

    private fun gameLoop() {
        lastFrameTime = glfwGetTime()

        while (!glfwWindowShouldClose(window)) {
            val currentTime = glfwGetTime()
            deltaTime = (currentTime - lastFrameTime).toFloat()
            lastFrameTime = currentTime

            fpsCounter++
            fpsTimer += deltaTime
            if (fpsTimer >= 1.0) {
                fps = fpsCounter
                fpsCounter = 0
                fpsTimer = 0.0
            }

            if (stateManager.isPlaying()) {
                if (glfwGetInputMode(window, GLFW_CURSOR) != GLFW_CURSOR_DISABLED) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_DISABLED)
                    // FIX: Use IntArray for glfwGetWindowSize and cast results to Double
                    val w = IntArray(1)
                    val h = IntArray(1)
                    glfwGetWindowSize(window, w, h)
                    lastX = w[0] / 2.0
                    lastY = h[0] / 2.0
                    glfwSetCursorPos(window, lastX, lastY)
                    firstMouse = true
                }
            } else {
                if (glfwGetInputMode(window, GLFW_CURSOR) != GLFW_CURSOR_NORMAL) {
                    glfwSetInputMode(window, GLFW_CURSOR, GLFW_CURSOR_NORMAL)
                }
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
                    renderGame()
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
        val skyColor = getSkyColor()
        glClearColor(skyColor[0], skyColor[1], skyColor[2], 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)

        shader.bind()

        val aspectRatio = if (height > 0) width.toFloat() / height.toFloat() else 1.0f
        shader.setUniform("projectionMatrix", camera.getProjectionMatrix(aspectRatio))
        shader.setUniform("viewMatrix", camera.getViewMatrix())
        shader.setUniform("modelMatrix", Matrix4f())
        shader.setUniform("textureSampler", 0)
        shader.setUniform("skyColor", skyColor[0], skyColor[1], skyColor[2])
        shader.setUniform("sunPosition", 1000f, 1000f, 1000f)
        shader.setUniform("ambientStrength", 0.4f)
        shader.setUniform("fogDensity", 0.007f)
        shader.setUniform("fogGradient", 1.5f)

        blockAtlas.bind()
        world.render(camera)
        shader.unbind()

        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)

        if (stateManager.isPlaying()) {
            uiRenderer.renderHUD(fps, camera.position)
        }

        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
    }

    private fun renderTitleScreen() {
        glClearColor(0.15f, 0.15f, 0.15f, 1.0f)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        uiRenderer.renderTitleScreen(
            menuNavigator.getMenuItems(),
            menuNavigator.selectedIndex
        )
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
    }

    private fun renderPauseMenu() {
        glDisable(GL_DEPTH_TEST)
        glDepthMask(false)
        uiRenderer.renderPauseMenu(
            menuNavigator.getMenuItems(),
            menuNavigator.selectedIndex
        )
        glDepthMask(true)
        glEnable(GL_DEPTH_TEST)
    }

    private fun getSkyColor(): FloatArray {
        return floatArrayOf(0.53f, 0.81f, 0.98f)
    }

    private fun cleanup() {
        shader.cleanup()
        blockAtlas.cleanup()
        world.cleanup()
        uiRenderer.cleanup()
        font.cleanup()

        glfwDestroyWindow(window)
        glfwTerminate()
        glfwSetErrorCallback(null)?.free()
    }

    override fun onStartSingleplayer(name: String, seed: Long) {
        println("Starting new world '$name' with seed $seed...")
        world = World() // Assuming world can take a seed now
        for (x in -4..4) {
            for (z in -4..4) {
                world.loadChunk(x, z)
            }
        }
        stateManager.startGame(name)
    }

    override fun onJoinServer(ip: String, port: Int) {
        println("Joining server at $ip:$port (Not Implemented)")
    }

    override fun onQuit() {
        glfwSetWindowShouldClose(window, true)
    }
}

fun main() {
    try {
        VexGame().run()
    } catch (e: Exception) {
        e.printStackTrace()
        exitProcess(-1)
    }
}