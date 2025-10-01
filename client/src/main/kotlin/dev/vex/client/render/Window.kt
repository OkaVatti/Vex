package dev.vex.client.render

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11

class Window(private val width: Int = 800, private val height: Int = 600, private val title: String = "Vex Client") {
    private var windowHandle: Long = 0

    fun init() {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!GLFW.glfwInit()) throw IllegalStateException("Unable to initialize GLFW")

        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)

        windowHandle = GLFW.glfwCreateWindow(width, height, title, 0, 0)
        if (windowHandle == 0L) throw RuntimeException("Failed to create GLFW window")

        GLFW.glfwMakeContextCurrent(windowHandle)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(windowHandle)

        // Setup OpenGL bindings
        GL.createCapabilities()
        GL11.glViewport(0, 0, width, height)
    }

    fun shouldClose(): Boolean = GLFW.glfwWindowShouldClose(windowHandle)

    fun pollEvents() = GLFW.glfwPollEvents()

    fun swapBuffers() = GLFW.glfwSwapBuffers(windowHandle)

    fun terminate() {
        GLFW.glfwDestroyWindow(windowHandle)
        GLFW.glfwTerminate()
        GLFW.glfwSetErrorCallback(null)?.free()
    }
}
