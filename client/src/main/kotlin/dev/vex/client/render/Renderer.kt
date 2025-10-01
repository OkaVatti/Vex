// client/src/main/kotlin/dev/vex/client/render/Renderer.kt
package dev.vex.client.render

import dev.vex.client.world.Chunk
import org.joml.Matrix4f
import org.joml.Vector3f

class Renderer {
    private val viewMatrix = Matrix4f()

    fun updateCamera(camera: Camera) {
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
        // Example: just print the chunk coords and top solid block
        println("Rendering chunk at (${chunk.position.x}, ${chunk.position.z})")
    }
}
