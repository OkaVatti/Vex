// client/src/main/kotlin/dev/vex/client/ClientMain.kt
package dev.vex.client

import dev.vex.client.render.Camera
import dev.vex.client.render.Renderer
import dev.vex.client.world.World

fun main() {
    val camera = Camera()
    val world = World()
    val renderer = Renderer()

    println("Starting client... use WASD to move (simulation)")

    // Simulated input loop
    while (true) {
        // Here you would read actual input
        camera.moveForward()  // just simulate movement forward
        renderer.updateCamera(camera)
        val visibleChunks = world.getVisibleChunks(camera.position.x, camera.position.z)
        renderer.renderWorld(visibleChunks)

        Thread.sleep(1000) // slow down loop for demo
    }
}
