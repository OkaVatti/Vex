package dev.vex.server.net.handlers

import dev.vex.client.render.Camera
import org.lwjgl.glfw.GLFW.*

/**
 * Minimal Input interface so handler compiles.
 * In your real server/client split you can replace this with your project's Input abstraction.
 */
interface Input {
    fun isKeyPressed(key: Int): Boolean
    fun getMouseDelta(): Pair<Float, Float>
}

/**
 * Server-side placeholder client handler that adapts Input -> Camera calls.
 * This is intentionally minimal so it compiles; replace Input with your real input type.
 */
class ClientHandler {
    fun handleInput(camera: Camera, input: Input, deltaTime: Float) {
        val forward = input.isKeyPressed(GLFW_KEY_W)
        val backward = input.isKeyPressed(GLFW_KEY_S)
        val left = input.isKeyPressed(GLFW_KEY_A)
        val right = input.isKeyPressed(GLFW_KEY_D)
        val jump = input.isKeyPressed(GLFW_KEY_SPACE)
        val sprint = input.isKeyPressed(GLFW_KEY_LEFT_SHIFT)
        val sneak = input.isKeyPressed(GLFW_KEY_LEFT_CONTROL)

        // drive camera using its processMovement API
        camera.processMovement(forward, backward, left, right, jump, sprint, sneak, deltaTime)

        // Mouse movement
        val (dx, dy) = input.getMouseDelta()
        // Use Camera's mouse movement method
        camera.processMouseMovement(dx * 0.1f, -dy * 0.1f)
    }
}
