package dev.vex.client.render

import dev.vex.client.world.Chunk
import org.joml.Matrix4f
import org.joml.Vector3f

// Note: This class appears to be unused in VexGame.kt, which uses camera.getViewMatrix() directly.
class Renderer {
    private val viewMatrix = Matrix4f()

    fun updateCamera(camera: Camera) {
        // *** FIX: Corrected the view matrix calculation. ***
        // The original implementation had incorrect rotation transforms that would cause
        // the world to "orbit" the origin. The correct FPS view matrix applies the
        // inverse of the camera's rotation, followed by the inverse of its translation.
        viewMatrix.identity()
        viewMatrix.rotateX(Math.toRadians(camera.pitch.toDouble()).toFloat())
        viewMatrix.rotateY(Math.toRadians(camera.yaw.toDouble()).toFloat())
        viewMatrix.translate(-camera.position.x, -camera.position.y, -camera.position.z)
    }

    fun renderWorld(chunks: List<Chunk>) {
        for (chunk in chunks) {
            renderChunk(chunk)
        }
    }

    private fun renderChunk(chunk: Chunk) {
        // *** FIX: Replaced slow and unsafe reflection with direct property access. ***
        // Assumes the Chunk class has public `x` and `z` properties.
        val coords = "(${chunk.x}, ${chunk.z})"

        // Example render stub — replace with the real GL draw calls.
        println("Rendering chunk at $coords")
    }
}