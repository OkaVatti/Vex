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
        // Try to print chunk coordinates if the chunk exposes a `position` with `x` and `z`.
        // Use reflection so this file doesn't depend on a specific Chunk API at compile time.
        val coords = try {
            val posField = chunk::class.java.getDeclaredField("position").apply { isAccessible = true }
            val pos = posField.get(chunk) ?: throw NoSuchFieldException("position is null")
            val xField = pos::class.java.getDeclaredField("x").apply { isAccessible = true }
            val zField = pos::class.java.getDeclaredField("z").apply { isAccessible = true }
            val x = xField.getInt(pos)
            val z = zField.getInt(pos)
            "($x, $z)"
        } catch (e: Exception) {
            // If the reflection fails, fallback to a simple toString so we still have useful output.
            chunk.toString()
        }

        // Example render stub — replace with the real GL draw calls.
        println("Rendering chunk at $coords")
    }
}
