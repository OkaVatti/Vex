package dev.vex.client.render

import dev.vex.client.world.Chunk
import org.joml.Matrix4f

/**
 * Legacy renderer class - currently unused in favor of direct rendering in VexGame.
 * Kept for potential future refactoring.
 *
 * Note: VexGame.kt uses camera.getViewMatrix() directly instead of this class.
 */
class Renderer {
    private val viewMatrix = Matrix4f()

    fun updateCamera(camera: Camera) {
        // Corrected view matrix calculation for FPS camera
        // Apply inverse rotation followed by inverse translation
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
        // Access chunk coordinates directly
        val coords = "(${chunk.x}, ${chunk.z})"

        // Placeholder for actual OpenGL rendering
        // Real implementation would use chunk meshes and shaders
        // println("Rendering chunk at $coords")
    }

    fun getViewMatrix(): Matrix4f = viewMatrix
}