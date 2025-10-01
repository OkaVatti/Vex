package dev.vex.server.net.handlers

// Placeholder for future request handling logic
class ClientHandler {
    fun handleInput(camera: Camera, input: Input) {
        if (input.isKeyPressed(KEY_W)) camera.moveForward()
        if (input.isKeyPressed(KEY_S)) camera.moveBackward()
        if (input.isKeyPressed(KEY_A)) camera.strafeLeft()
        if (input.isKeyPressed(KEY_D)) camera.strafeRight()

        // Mouse movement
        val (dx, dy) = input.getMouseDelta()
        camera.rotate(dx * 0.1f, -dy * 0.1f)
    }
}
